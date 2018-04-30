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

import com.edduarte.similarity.Similarity;
import com.edduarte.similarity.StringSimilarity;
import com.edduarte.similarity.converter.KShingler;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class JaccardStringSimilarity extends StringSimilarity {

  protected final KShingler kShingler;

  protected final ExecutorService exec;


  /**
   * Instantiates a Similarity class for strings using the Jaccard algorithm.
   *
   * @param k    the length k of the shingles to generate
   * @param exec the executor that will receive the concurrent shingle
   *             processing tasks
   */
  public JaccardStringSimilarity(
      String s1,
      String s2,
      int k,
      ExecutorService exec) {
    super(s1, s2);
    Objects.requireNonNull(exec, "Executor must not be null");
    this.kShingler = new KShingler(k);
    this.exec = exec;
  }


  @Override
  public double getAsDouble() {
    String s1 = getFirst();
    String s2 = getSecond();
    ShinglePair shingles = getShingles(s1, s2);
    return Similarity.jaccardIndex(
        shingles.getShingles1(),
        shingles.getShingles2()
    );
  }


  protected ShinglePair getShingles(String s1, String s2) {
    Future<List<CharSequence>> future1 = exec.submit(kShingler.apply(s1));
    Future<List<CharSequence>> future2 = exec.submit(kShingler.apply(s2));

    try {
      List<CharSequence> shingles1 = future1.get();
      List<CharSequence> shingles2 = future2.get();
      return new ShinglePair(shingles1, shingles2);
    } catch (ExecutionException | InterruptedException ex) {
      String m = "There was a problem processing shingles.";
      throw new RuntimeException(m, ex);
    } finally {
      future1 = null;
      future2 = null;
    }
  }


  protected static class ShinglePair {

    private final List<CharSequence> shingles1;

    private final List<CharSequence> shingles2;


    protected ShinglePair(
        List<CharSequence> shingles1,
        List<CharSequence> shingles2) {
      this.shingles1 = shingles1;
      this.shingles2 = shingles2;
    }


    public List<CharSequence> getShingles1() {
      return shingles1;
    }


    public List<CharSequence> getShingles2() {
      return shingles2;
    }
  }
}
