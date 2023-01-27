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
package org.apache.solr.spelling;

import org.apache.solr.common.util.NamedList;

public class SpellCheckCollation implements Comparable<SpellCheckCollation> {
  private NamedList<String> misspellingsAndCorrections;
  private long hits;
  private float maxScore;
  private String sortMaxScore;
  private int internalRank;
  private String collationQuery;

  @Override
  public int compareTo(SpellCheckCollation scc) {
    if ("desc".equals(sortMaxScore)) {
      int s = Float.compare(scc.maxScore, maxScore);
      if (s != 0) return s;
    }
    if ("asc".equals(sortMaxScore)) {
      int s = Float.compare(maxScore, scc.maxScore);
      if (s != 0) return s;
    }
    int c = Integer.compare(internalRank, scc.internalRank);
    if (c == 0) {
      return collationQuery.compareTo(scc.collationQuery);
    }
    return c;
  }

  public NamedList<String> getMisspellingsAndCorrections() {
    return misspellingsAndCorrections;
  }

  public void setMisspellingsAndCorrections(NamedList<String> misspellingsAndCorrections) {
    this.misspellingsAndCorrections = misspellingsAndCorrections;
  }

  public long getHits() {
    return hits;
  }

  public void setHits(long hits) {
    this.hits = hits;
  }

  public float getMaxScore() { return maxScore; }

  public void setMaxScore(float maxScore) { this.maxScore = maxScore; }

  public String getCollationQuery() {
    return collationQuery;
  }

  public void setCollationQuery(String collationQuery) {
    this.collationQuery = collationQuery;
  }

  public int getInternalRank() {
    return internalRank;
  }

  public void setInternalRank(int internalRank) {
    this.internalRank = internalRank;
  }

  public void setSortMaxScore(String sortMaxScore) {
    this.sortMaxScore = sortMaxScore;
  }
}
