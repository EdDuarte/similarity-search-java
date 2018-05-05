package com.edduarte.similarity;

import com.edduarte.similarity.impl.MinHashSetSimilarity;
import com.edduarte.similarity.impl.MinHashStringSimilarity;
import orestes.bloomfilter.HashProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public final class MinHashFactory extends Factory {

  private int k;

  private int n;

  private int sigSize;

  private HashProvider.HashMethod h;


  MinHashFactory() {
    super();
    // sensible defaults for common small strings (smaller than an email)
    // or small collections (between 10 to 40 elements)
    this.k = 2;
    this.n = -1;
    this.sigSize = 100;
    this.h = HashProvider.HashMethod.Murmur3;
  }


  /**
   * Length of n-gram shingles that are used for comparison (used for strings only).
   */
  public synchronized MinHashFactory withShingleLength(int shingleLength) {
    this.k = shingleLength;
    return this;
  }


  /**
   * Number of unique elements in both sets (used for sets only). For example, if set1=[4, 5, 6, 7,
   * 8] and set2=[7, 8, 9, 10], this value should be 7. If nothing is provided, this value is
   * determined in pre-processing.
   */
  public synchronized MinHashFactory withNumberOfElements(int elementCount) {
    this.n = elementCount;
    return this;
  }


  /**
   * The size of the generated signatures, which are compared to determine similarity.
   */
  public synchronized MinHashFactory withSignatureSize(int signatureSize) {
    this.sigSize = signatureSize;
    return this;
  }


  /**
   * The hashing algorithm used to hash shingles to signatures (used for strings only).
   */
  public synchronized MinHashFactory withHashMethod(
      HashProvider.HashMethod hashMethod) {
    this.h = hashMethod;
    return this;
  }


  /**
   * An executor where the kshingling and signature processing tasks are spawned. If nothing is
   * provided then it launches a new executor with the cached thread pool.
   */
  public synchronized MinHashFactory withExecutor(ExecutorService executor) {
    setExec(executor);
    return this;
  }


  @Override
  StringSimilarity initStringSimilarityTask(
      String s1, String s2, ExecutorService exec) {
    return new MinHashStringSimilarity(s1, s2, k, sigSize, h, exec);
  }


  @Override
  SetSimilarity initSetSimilarityTask(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2,
      ExecutorService exec) {
    int nAux = n;
    if (nAux < 0) {
      Set<Number> unionSet = new HashSet<>(c1);
      unionSet.addAll(c2);
      nAux = (int) unionSet.stream().distinct().count();
    }
    return new MinHashSetSimilarity(c1, c2, nAux, sigSize, exec);
  }
}
