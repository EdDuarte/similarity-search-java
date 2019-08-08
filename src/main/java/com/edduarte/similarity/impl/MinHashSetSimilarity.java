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

package com.edduarte.similarity.impl;

import com.edduarte.similarity.SetSimilarity;
import com.edduarte.similarity.Similarity;
import com.edduarte.similarity.converter.SetToSignatureConverter;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class MinHashSetSimilarity extends SetSimilarity {

  protected final SetToSignatureConverter p;

  protected final ExecutorService exec;


  /**
   * Instantiates a Similarity class for number sets using the MinHashing algorithm.
   *
   * @param exec the executor that will receive the concurrent shingle processing tasks
   * @param n the total number of unique elements in both sets
   * @param sigSize the length of the signature array to be generated
   */
  public MinHashSetSimilarity(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2,
      int n,
      int sigSize,
      ExecutorService exec) {
    super(c1, c2);
    Objects.requireNonNull(exec, "Executor must not be null");
    this.p = new SetToSignatureConverter(n, sigSize);
    this.exec = exec;
  }


  @Override
  public double getAsDouble() {
    Collection<? extends Number> c1 = getFirst();
    Collection<? extends Number> c2 = getSecond();
    Future<int[]> signatureFuture1 = exec.submit(p.apply(c1));
    Future<int[]> signatureFuture2 = exec.submit(p.apply(c2));

    try {
      int[] signature1 = signatureFuture1.get();
      int[] signature2 = signatureFuture2.get();

      return Similarity.signatureIndex(signature1, signature2);

    } catch (ExecutionException | InterruptedException ex) {
      String m = "There was a problem processing set signatures.";
      throw new RuntimeException(m, ex);
    } finally {
      signatureFuture1 = null;
      signatureFuture2 = null;
    }
  }
}
