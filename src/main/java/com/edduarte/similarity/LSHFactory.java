package com.edduarte.similarity;

import com.edduarte.similarity.internal.LSHSetSimilarity;
import com.edduarte.similarity.internal.LSHStringSimilarity;
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
public final class LSHFactory extends Factory {

  private int k;

  private int n;

  private int b;

  private int r;

  private double s;

  private HashProvider.HashMethod h;


  LSHFactory() {
    super();
    // sensible defaults for common small strings (smaller than an email)
    // or small collections (between 10 to 40 elements)
    this.k = 2;
    this.n = -1;
    this.b = 20;
    this.r = 5;
    this.s = 0.5;
    this.h = HashProvider.HashMethod.Murmur3;
  }


  /**
   * Length of n-gram shingles that are used when generating signatures
   * (used for strings only).
   */
  public synchronized LSHFactory withShingleLength(int shingleLength) {
    this.k = shingleLength;
    return this;
  }


  /**
   * Number of unique elements in both sets (used for sets only). For
   * example, if set1=[4, 5, 6, 7, 8] and set2=[7, 8, 9, 10], this value
   * should be 7. If nothing is provided, this value is determined in
   * pre-processing.
   */
  public synchronized LSHFactory withNumberOfElements(int elementCount) {
    this.n = elementCount;
    return this;
  }


  /**
   * The number of bands where the minhash signatures will be structured.
   */
  public synchronized LSHFactory withNumberOfBands(int bandCount) {
    this.b = bandCount;
    return this;
  }


  /**
   * The number of rows where the minhash signatures will be structured.
   */
  public synchronized LSHFactory withNumberOfRows(int rowCount) {
    this.r = rowCount;
    return this;
  }


  /**
   * A threshold S that balances the number of false positives and false
   * negatives.
   */
  public synchronized LSHFactory withThreshold(double threshold) {
    this.s = threshold;
    return this;
  }


  /**
   * The hashing algorithm used to hash shingles to signatures (used for
   * strings only).
   */
  public synchronized LSHFactory withHashMethod(HashProvider.HashMethod hashMethod) {
    this.h = hashMethod;
    return this;
  }


  /**
   * An executor where the kshingling and signature processing tasks are
   * spawned. If nothing is provided then it launches a new executor with
   * the cached thread pool.
   */
  public synchronized LSHFactory withExecutor(ExecutorService executor) {
    setExec(executor);
    return this;
  }


  @Override
  StringSimilarity initStringSimilarityTask(
      String s1, String s2, ExecutorService exec) {
    return new LSHStringSimilarity(s1, s2, b, r, s, h, k, exec);
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
    return new LSHSetSimilarity(c1, c2, nAux, b, r, s, exec);
  }
}
