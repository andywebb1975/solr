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
package org.apache.solr.search.similarities;

import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.SimilarityFactory;

/**
 * Factory for {@link LMDirichletSimilarity}
 *
 * <p>Parameters:
 *
 * <ul>
 *   <li>parameter mu (float): smoothing parameter &mu;. The default is <code>2000</code>
 * </ul>
 *
 * <p>Optional settings:
 *
 * <ul>
 *   <li>discountOverlaps (bool): Sets {link Similarity#getDiscountOverlaps()}
 * </ul>
 *
 * @lucene.experimental
 */
public class LMDirichletSimilarityFactory extends SimilarityFactory {
  private boolean discountOverlaps;
  private Float mu;

  @Override
  public void init(SolrParams params) {
    super.init(params);
    discountOverlaps = params.getBool("discountOverlaps", true);
    mu = params.getFloat("mu");
  }

  @Override
  public Similarity getSimilarity() {

    // Default μ is 2000 in Lucene. Unfortunately, there is no constant we can use
    if (mu == null) {
      mu = 2000f;
    }

    LMSimilarity.CollectionModel model = new LMSimilarity.DefaultCollectionModel();
    return new LMDirichletSimilarity(model, discountOverlaps, mu);
  }
}
