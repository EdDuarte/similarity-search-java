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


import com.edduarte.similarity.Similarity;
import com.edduarte.similarity.StringSimilarity;
import com.edduarte.similarity.converter.KShingles2SignatureConverter;
import orestes.bloomfilter.HashProvider.HashMethod;

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
public class MinHashStringSimilarity extends StringSimilarity {

  protected final ExecutorService exec;

  protected final JaccardStringSimilarity jaccard;

  protected final KShingles2SignatureConverter p;


  /**
   * Instantiates a Similarity class for strings using the MinHashing algorithm.
   *
   * @param k the length k of the shingles to generate
   * @param sigSize the length of the signature array to be generated
   * @param hash the hash method to use when hashing shingles to signatures
   * @param exec the executor that will receive the concurrent shingle processing tasks
   */
  public MinHashStringSimilarity(
      String s1,
      String s2,
      int k,
      int sigSize,
      HashMethod hash,
      ExecutorService exec) {
    super(s1, s2);
    Objects.requireNonNull(hash, "Hash method must not be null");
    Objects.requireNonNull(exec, "Executor must not be null");
    this.jaccard = new JaccardStringSimilarity(s1, s2, k, exec);
    this.p = new KShingles2SignatureConverter(hash, sigSize);
    this.exec = exec;
  }


  @Override
  public double getAsDouble() {
    String s1 = getFirst();
    String s2 = getSecond();
    JaccardStringSimilarity.ShinglePair p = jaccard.getShingles(s1, s2);
    int[][] signatures = getSignatures(p.getShingles1(), p.getShingles2());
    return Similarity.signatureIndex(signatures[0], signatures[1]);
  }


  protected int[][] getSignatures(List<CharSequence> shingles1, List<CharSequence> shingles2) {
    Future<int[]> signatureFuture1 = exec.submit(p.apply(shingles1));
    Future<int[]> signatureFuture2 = exec.submit(p.apply(shingles2));

    try {
      int[] signature1 = signatureFuture1.get();
      int[] signature2 = signatureFuture2.get();
      int signatureSize = signature1.length;
      int[][] result = new int[2][signatureSize];
      result[0] = signature1;
      result[1] = signature2;
      return result;

    } catch (ExecutionException | InterruptedException ex) {
      String m = "There was a problem processing shingle signatures.";
      throw new RuntimeException(m, ex);
    } finally {
      signatureFuture1 = null;
      signatureFuture2 = null;
    }
  }
}
