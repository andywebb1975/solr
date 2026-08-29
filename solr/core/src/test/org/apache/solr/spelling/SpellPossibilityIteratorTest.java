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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.solr.SolrTestCaseJ4;
import org.junit.Before;
import org.junit.Test;

public class SpellPossibilityIteratorTest extends SolrTestCaseJ4 {
  private static final Token TOKEN_AYE = new Token("AYE", 0, 3);
  private static final Token TOKEN_BEE = new Token("BEE", 4, 7);
  private static final Token TOKEN_AYE_BEE = new Token("AYE BEE", 0, 7);
  private static final Token TOKEN_CEE = new Token("CEE", 8, 11);

  private LinkedHashMap<String, SuggestWord> AYE;
  private LinkedHashMap<String, SuggestWord> BEE;
  private LinkedHashMap<String, SuggestWord> AYE_BEE;
  private LinkedHashMap<String, SuggestWord> CEE;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    SuggestWord sw = new SuggestWord();
    sw.freq = 0;

    AYE = new LinkedHashMap<>();
    AYE.put("I", sw);
    AYE.put("II", sw);
    AYE.put("III", sw);
    AYE.put("IV", sw);
    AYE.put("V", sw);
    AYE.put("VI", sw);
    AYE.put("VII", sw);
    AYE.put("VIII", sw);

    BEE = new LinkedHashMap<>();
    BEE.put("alpha", sw);
    BEE.put("beta", sw);
    BEE.put("gamma", sw);
    BEE.put("delta", sw);
    BEE.put("epsilon", sw);
    BEE.put("zeta", sw);
    BEE.put("eta", sw);
    BEE.put("theta", sw);
    BEE.put("iota", sw);

    AYE_BEE = new LinkedHashMap<>();
    AYE_BEE.put("one-alpha", sw);
    AYE_BEE.put("two-beta", sw);
    AYE_BEE.put("three-gamma", sw);
    AYE_BEE.put("four-delta", sw);
    AYE_BEE.put("five-epsilon", sw);
    AYE_BEE.put("six-zeta", sw);
    AYE_BEE.put("seven-eta", sw);
    AYE_BEE.put("eight-theta", sw);
    AYE_BEE.put("nine-iota", sw);

    CEE = new LinkedHashMap<>();
    CEE.put("one", sw);
    CEE.put("two", sw);
    CEE.put("three", sw);
    CEE.put("four", sw);
    CEE.put("five", sw);
    CEE.put("six", sw);
    CEE.put("seven", sw);
    CEE.put("eight", sw);
    CEE.put("nine", sw);
    CEE.put("ten", sw);
  }

  @Test
  public void testScalability() {
    Map<Token, LinkedHashMap<String, SuggestWord>> lotsaSuggestions = new LinkedHashMap<>();
    lotsaSuggestions.put(TOKEN_AYE, AYE);
    lotsaSuggestions.put(TOKEN_BEE, BEE);
    lotsaSuggestions.put(TOKEN_CEE, CEE);

    lotsaSuggestions.put(new Token("AYE1", 0, 3), AYE);
    lotsaSuggestions.put(new Token("BEE1", 4, 7), BEE);
    lotsaSuggestions.put(new Token("CEE1", 8, 11), CEE);

    lotsaSuggestions.put(new Token("AYE2", 0, 3), AYE);
    lotsaSuggestions.put(new Token("BEE2", 4, 7), BEE);
    lotsaSuggestions.put(new Token("CEE2", 8, 11), CEE);

    lotsaSuggestions.put(new Token("AYE3", 0, 3), AYE);
    lotsaSuggestions.put(new Token("BEE3", 4, 7), BEE);
    lotsaSuggestions.put(new Token("CEE3", 8, 11), CEE);

    lotsaSuggestions.put(new Token("AYE4", 0, 3), AYE);
    lotsaSuggestions.put(new Token("BEE4", 4, 7), BEE);
    lotsaSuggestions.put(new Token("CEE4", 8, 11), CEE);

    PossibilityIterator iter = new PossibilityIterator(lotsaSuggestions, 1000, 10000, false);
    int count = 0;
    while (iter.hasNext()) {
      PossibilityIterator.RankedSpellPossibility rsp = iter.next();
      count++;
    }
    assertEquals(1000, count);

    lotsaSuggestions.put(new Token("AYE_BEE1", 0, 7), AYE_BEE);
    lotsaSuggestions.put(new Token("AYE_BEE2", 0, 7), AYE_BEE);
    lotsaSuggestions.put(new Token("AYE_BEE3", 0, 7), AYE_BEE);
    lotsaSuggestions.put(new Token("AYE_BEE4", 0, 7), AYE_BEE);
    iter = new PossibilityIterator(lotsaSuggestions, 1000, 10000, true);
    count = 0;
    while (iter.hasNext()) {
      PossibilityIterator.RankedSpellPossibility rsp = iter.next();
      count++;
    }
    assertTrue(count < 100);
  }

  @Test
  public void testSpellPossibilityIterator() {
    Map<Token, LinkedHashMap<String, SuggestWord>> suggestions = new LinkedHashMap<>();
    suggestions.put(TOKEN_AYE, AYE);
    suggestions.put(TOKEN_BEE, BEE);
    suggestions.put(TOKEN_CEE, CEE);

    PossibilityIterator iter = new PossibilityIterator(suggestions, 1000, 10000, false);
    int count = 0;
    while (iter.hasNext()) {

      PossibilityIterator.RankedSpellPossibility rsp = iter.next();
      if (count == 0) {
        assertEquals("I", rsp.corrections.get(0).getCorrection());
        assertEquals("alpha", rsp.corrections.get(1).getCorrection());
        assertEquals("one", rsp.corrections.get(2).getCorrection());
      }
      count++;
    }
    assertEquals(
        ("Three maps (8*9*10) should return 720 iterations but instead returned " + count),
        720,
        count);

    suggestions.remove(TOKEN_CEE);
    iter = new PossibilityIterator(suggestions, 100, 10000, false);
    count = 0;
    while (iter.hasNext()) {
      iter.next();
      count++;
    }
    assertEquals(
        ("Two maps (8*9) should return 72 iterations but instead returned " + count), 72, count);

    suggestions.remove(TOKEN_BEE);
    iter = new PossibilityIterator(suggestions, 5, 10000, false);
    count = 0;
    while (iter.hasNext()) {
      iter.next();
      count++;
    }
    assertEquals(("We requested 5 suggestions but got " + count), 5, count);

    suggestions.remove(TOKEN_AYE);
    iter = new PossibilityIterator(suggestions, Integer.MAX_VALUE, 10000, false);
    count = 0;
    while (iter.hasNext()) {
      iter.next();
      count++;
    }
    assertEquals(("No maps should return 0 iterations but instead returned " + count), 0, count);
  }

  @Test
  public void testOverlappingTokens() {
    Map<Token, LinkedHashMap<String, SuggestWord>> overlappingSuggestions = new LinkedHashMap<>();
    overlappingSuggestions.put(TOKEN_AYE, AYE);
    overlappingSuggestions.put(TOKEN_BEE, BEE);
    overlappingSuggestions.put(TOKEN_AYE_BEE, AYE_BEE);
    overlappingSuggestions.put(TOKEN_CEE, CEE);

    PossibilityIterator iter =
        new PossibilityIterator(overlappingSuggestions, Integer.MAX_VALUE, Integer.MAX_VALUE, true);
    int aCount = 0;
    int abCount = 0;
    Set<PossibilityIterator.RankedSpellPossibility> dupChecker = new HashSet<>();
    while (iter.hasNext()) {
      PossibilityIterator.RankedSpellPossibility rsp = iter.next();
      Token a = null;
      Token b = null;
      Token ab = null;
      Token c = null;
      for (SpellCheckCorrection scc : rsp.corrections) {
        if (scc.getOriginal().equals(TOKEN_AYE)) {
          a = scc.getOriginal();
        } else if (scc.getOriginal().equals(TOKEN_BEE)) {
          b = scc.getOriginal();
        } else if (scc.getOriginal().equals(TOKEN_AYE_BEE)) {
          ab = scc.getOriginal();
        } else if (scc.getOriginal().equals(TOKEN_CEE)) {
          c = scc.getOriginal();
        }
        if (ab != null) {
          abCount++;
        } else {
          aCount++;
        }
      }
      assertNotNull(c);
      assertTrue(ab != null || (a != null && b != null));
      assertTrue(ab == null || (a == null && b == null));
      assertTrue(dupChecker.add(rsp));
    }
    assertEquals(2160, aCount);
    assertEquals(180, abCount);
  }
}
