package com.edduarte.similarity;

import com.edduarte.similarity.hash.HashProvider;
import com.edduarte.similarity.internal.LSHSetSimilarity;
import com.edduarte.similarity.internal.LSHStringSimilarity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public final class LSHFactory extends Factory {

  static final LSHFactory SINGLETON = new LSHFactory();

  private final int k;

  private final int n;

  private final int b;

  private final int r;

  private final double s;

  private final HashProvider.HashMethod h;


  private LSHFactory() {
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


  private LSHFactory(
      int k,
      int n,
      int b,
      int r,
      double s,
      HashProvider.HashMethod h,
      ExecutorService exec) {
    super(exec);
    this.k = k;
    this.n = n;
    this.b = b;
    this.r = r;
    this.s = s;
    this.h = h;
  }


  public LSHFactory with(
      int shingleLength,
      int elementCount,
      int bandCount,
      int rowCount,
      double threshold,
      HashProvider.HashMethod hashMethod,
      ExecutorService executor) {
    return new LSHFactory(
        shingleLength,
        elementCount,
        bandCount,
        rowCount,
        threshold,
        hashMethod,
        executor
    );
  }


  /**
   * Length of n-gram shingles that are used when generating signatures
   * (used for strings only).
   */
  public LSHFactory withShingleLength(int shingleLength) {
    return new LSHFactory(shingleLength, n, b, r, s, h, exec);
  }


  /**
   * Number of unique elements in both sets (used for sets only). For
   * example, if set1=[4, 5, 6, 7, 8] and set2=[7, 8, 9, 10], this value
   * should be 7. If nothing is provided, this value is determined in
   * pre-processing.
   */
  public LSHFactory withNumberOfElements(int elementCount) {
    return new LSHFactory(k, elementCount, b, r, s, h, exec);
  }


  /**
   * The number of bands where the minhash signatures will be structured.
   */
  public LSHFactory withNumberOfBands(int bandCount) {
    return new LSHFactory(k, n, bandCount, r, s, h, exec);
  }


  /**
   * The number of rows where the minhash signatures will be structured.
   */
  public LSHFactory withNumberOfRows(int rowCount) {
    return new LSHFactory(k, n, b, rowCount, s, h, exec);
  }


  /**
   * A threshold S that balances the number of false positives and false
   * negatives.
   */
  public LSHFactory withThreshold(double threshold) {
    return new LSHFactory(k, n, b, r, threshold, h, exec);
  }


  /**
   * The hashing algorithm used to hash shingles to signatures (used for
   * strings only).
   */
  public LSHFactory withHashMethod(HashProvider.HashMethod hashMethod) {
    return new LSHFactory(k, n, b, r, s, hashMethod, exec);
  }


  /**
   * An executor where the kshingling and signature processing tasks are
   * spawned. If nothing is provided then it launches a new executor with
   * the cached thread pool.
   */
  public LSHFactory withExecutor(ExecutorService executor) {
    return new LSHFactory(k, n, b, r, s, h, executor);
  }


  public double of(String s1, String s2) {
    ExecutorService e = exec;
    boolean usingDefaultExec = false;
    if (e == null || e.isShutdown()) {
      e = Executors.newCachedThreadPool();
      usingDefaultExec = true;
    }
    LSHStringSimilarity j = new LSHStringSimilarity(e, b, r, s, h, k);
    double index = j.calculate(s1, s2);
    if (usingDefaultExec) {
      closeExecutor();
    }
    return index;
  }


  public double of(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    ExecutorService e = exec;
    boolean usingDefaultExec = false;
    if (e == null || e.isShutdown()) {
      e = Executors.newCachedThreadPool();
      usingDefaultExec = true;
    }
    int nAux = n;
    if (nAux < 0) {
      Set<Number> unionSet = new HashSet<>(c1);
      unionSet.addAll(c2);
      nAux = (int) unionSet.parallelStream().distinct().count();
    }
    LSHSetSimilarity j = new LSHSetSimilarity(e, nAux, b, r, s);
    double index = j.calculate(c1, c2);
    if (usingDefaultExec) {
      closeExecutor();
    }
    return index;
  }
}
