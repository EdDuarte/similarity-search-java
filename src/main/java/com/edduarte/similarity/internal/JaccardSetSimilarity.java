/*
 * Copyright 2017 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.similarity.internal;

import com.edduarte.similarity.SetSimilarity;
import com.edduarte.similarity.Similarity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class JaccardSetSimilarity implements SetSimilarity {


  /**
   * Instantiates a Similarity class for number sets using the Jaccard
   * algorithm.
   */
  public JaccardSetSimilarity() {
  }


  public double calculate(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    Set<Number> intersectionSet = new HashSet<>();
    for (Number number : c1) {
      if (c2.contains(number)) {
        intersectionSet.add(number);
      }
    }
    Set<Number> unionSet = new HashSet<>(c1);
    unionSet.addAll(c2);
    return Similarity.jaccardIndex(intersectionSet.size(), unionSet.size());
  }
}
