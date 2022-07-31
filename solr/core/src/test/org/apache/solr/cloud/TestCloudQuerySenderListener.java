/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.cloud;

import static java.util.Arrays.asList;
import static org.apache.solr.common.util.Utils.fromJSONString;
import static org.apache.solr.common.util.Utils.getObjectByPath;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudLegacySolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.util.LogLevel;
import org.apache.solr.util.TestInjection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tests related to SOLR-9359, based on TestCloudSearcherWarming */
@LogLevel(
    "org.apache.solr.cloud.overseer.*=DEBUG,org.apache.solr.cloud.Overseer=DEBUG,org.apache.solr.cloud.ZkController=DEBUG")
public class TestCloudQuerySenderListener extends SolrCloudTestCase {
  public static final AtomicReference<String> coreNodeNameRef = new AtomicReference<>(null),
      coreNameRef = new AtomicReference<>(null);
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final AtomicInteger sleepTime = new AtomicInteger(-1);
  private static final String configSet = "cloud-minimal-caches";

  @BeforeClass
  public static void setupCluster() throws Exception {
    // necessary to find the index+tlog intact after restart
    useFactory("solr.StandardDirectoryFactory");
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    configureCluster(1).addConfig("conf", configset(configSet)).configure();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    coreNameRef.set(null);
    coreNodeNameRef.set(null);
    sleepTime.set(-1);

    if (null != cluster) {
      cluster.deleteAllCollections();
      cluster.deleteAllConfigSets();
      cluster.shutdown();
      cluster = null;
    }
    TestInjection.wrongIndexFingerprint = null;

    super.tearDown();
  }

  @Test
  public void testQuerySenderListener() throws Exception {

    CloudSolrClient solrClient = cluster.getSolrClient();

    SolrZkClient client = zkClient();
    client.upConfig(configset(configSet), configSet);

    String collectionName = "testQuerySenderListener";
    CollectionAdminRequest.Create create =
        CollectionAdminRequest.createCollection(collectionName, configSet, 1, 1)
            .setCreateNodeSet(cluster.getJettySolrRunner(0).getNodeName());
    create.process(solrClient);

    cluster.waitForActiveCollection(collectionName, 1, 1);

    solrClient.setDefaultCollection(collectionName);

    String addListenerCommand =
        "{'add-listener' : {"
	+ "'name':'newSearcherListener','event':'newSearcher',"
        + "'class':'solr.QuerySenderListener',"
        + "'queries':["
	+ "{'q':'*','fq':'id:1'}"
	+ "{'q':'*','fq':'id:2'}"
	+ "]"
        + "}}";

    ConfigRequest request = new ConfigRequest(addListenerCommand);
    solrClient.request(request);

    solrClient.add(new SolrInputDocument("id", "1"));
    solrClient.commit();

    QueryResponse response = solrClient.query(new SolrQuery("id:1"));
    assertEquals(1, response.getResults().getNumFound());

    Set<String> liveNodes = solrClient.getClusterState().getLiveNodes();
    if (liveNodes.isEmpty())
      fail("No live nodes found!");
    String firstLiveNode = liveNodes.iterator().next();
    String solrUrl = ZkStateReader.from(solrClient).getBaseUrlForNodeName(firstLiveNode);

    // check filter cache has two entries, driven by the warming queries
    String uri = solrUrl + "/" + collectionName + "/admin/mbeans?stats=true";

    Map<?, ?> respMap = getAsMap(solrClient, uri);

    ArrayList<?> mbeans = (ArrayList)respMap.get("solr-mbeans");

    /*

     TODO expecting mbeans to include this
     
     "CACHE",
     {
       "filterCache": {
          "stats": {
             "CACHE.searcher.filterCache.size": 2,


     ... but I get stats={CACHE.searcher.filterCache=null}}

     If I use the cloud-minimal-caches configset in a separate Solr and make a collection the caches are seen as expected.
     
    */

    // this gives me time to determine the port number and inspect the Solr instance in a browser
    // ./gradlew -p solr/core test --tests TestCloudQuerySenderListener
    // netstat -lnp | grep java
    // http://localhost:nnnnn/solr/testQuerySenderListener/admin/mbeans?stats=true
    // Thread.sleep(120000);

    fail(mbeans.toString());

  }

  private Map<?, ?> getAsMap(CloudSolrClient cloudClient, String uri) throws Exception {
    HttpGet get = new HttpGet(uri);
    HttpEntity entity = null;
    try {
      entity = ((CloudLegacySolrClient) cloudClient).getHttpClient().execute(get).getEntity();
      String response = EntityUtils.toString(entity, StandardCharsets.UTF_8);
      return (Map<?, ?>) fromJSONString(response);
    } finally {
      EntityUtils.consumeQuietly(entity);
    }
  }
}
