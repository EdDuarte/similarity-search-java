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
import com.edduarte.similarity.converter.Set2SignatureConverter;
import com.edduarte.similarity.converter.Signature2BandsConverter;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class LSHSetSimilarity implements SetSimilarity {

  protected final JaccardSetSimilarity jaccard;

  protected final Set2SignatureConverter sigp;

  protected final Signature2BandsConverter bandp;

  protected final ExecutorService exec;


  /**
   * Instantiates a Similarity class for number sets using the LSH algorithm.
   *
   * @param exec the executor that will receive the concurrent signature and
   *             band processing tasks
   * @param n    the total number of unique elements in both sets
   * @param b    the number of bands
   * @param r    the number of rows
   * @param s    the threshold (value between 0.0 and 1.0) that balances the
   *             trade-off between the number of false positives and false
   *             negatives. A sensible threshold is 0.5, so we have a equal
   *             number of false positives and false negatives.
   */
  public LSHSetSimilarity(ExecutorService exec, int n, int b, int r, double s) {
    // signature size is determined by a threshold S
    this.exec = exec;
    int R = (int) Math.ceil(Math.log(1.0 / b) / Math.log(s)) + 1;
    int sigSize = R * b;
    this.jaccard = new JaccardSetSimilarity();
    this.sigp = new Set2SignatureConverter(n, sigSize);
    this.bandp = new Signature2BandsConverter(b, r);
  }


  @Override
  public double calculate(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    return isCandidatePair(c1, c2) ? jaccard.calculate(c1, c2) : 0;
  }


  protected boolean isCandidatePair(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    try {
      Future<int[]> signatureFuture1 = exec.submit(sigp.apply(c1));
      Future<int[]> signatureFuture2 = exec.submit(sigp.apply(c2));

      int[] signature1 = signatureFuture1.get();
      int[] signature2 = signatureFuture2.get();

      Future<int[]> bandsFuture1 = exec.submit(bandp.apply(signature1));
      Future<int[]> bandsFuture2 = exec.submit(bandp.apply(signature2));

      int[] bands1 = bandsFuture1.get();
      int[] bands2 = bandsFuture2.get();

      return Similarity.isCandidatePair(bands1, bands2);

    } catch (ExecutionException | InterruptedException ex) {
      String m = "There was a problem processing set signatures.";
      throw new RuntimeException(m, ex);
    }
  }
}
