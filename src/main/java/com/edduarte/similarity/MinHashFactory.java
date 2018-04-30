package com.edduarte.similarity;

import com.edduarte.similarity.hash.HashProvider;
import com.edduarte.similarity.internal.MinHashSetSimilarity;
import com.edduarte.similarity.internal.MinHashStringSimilarity;

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
public final class MinHashFactory extends Factory {

  static final MinHashFactory SINGLETON = new MinHashFactory();

  private final int k;

  private final int n;

  private final int sigSize;

  private final HashProvider.HashMethod h;


  private MinHashFactory() {
    super();
    // sensible defaults for common small strings (smaller than an email)
    // or small collections (between 10 to 40 elements)
    this.k = 2;
    this.n = -1;
    this.sigSize = 100;
    this.h = HashProvider.HashMethod.Murmur3;
  }


  private MinHashFactory(
      int k,
      int n,
      int sigSize,
      HashProvider.HashMethod h,
      ExecutorService exec) {
    super(exec);
    this.k = k;
    this.n = n;
    this.sigSize = sigSize;
    this.h = h;
  }


  public MinHashFactory with(
      int shingleLength,
      int elementCount,
      int signatureSize,
      HashProvider.HashMethod hashMethod,
      ExecutorService executor) {
    return new MinHashFactory(
        shingleLength,
        elementCount,
        signatureSize,
        hashMethod,
        executor
    );
  }


  /**
   * Length of n-gram shingles that are used for comparison (used for
   * strings only).
   */
  public MinHashFactory withShingleLength(int shingleLength) {
    return new MinHashFactory(shingleLength, n, sigSize, h, exec);
  }


  /**
   * Number of unique elements in both sets (used for sets only). For
   * example, if set1=[4, 5, 6, 7, 8] and set2=[7, 8, 9, 10], this value
   * should be 7. If nothing is provided, this value is determined in
   * pre-processing.
   */
  public MinHashFactory withNumberOfElements(int elementCount) {
    return new MinHashFactory(k, elementCount, sigSize, h, exec);
  }


  /**
   * The size of the generated signatures, which are compared to determine
   * similarity.
   */
  public MinHashFactory withSignatureSize(int signatureSize) {
    return new MinHashFactory(k, n, signatureSize, h, exec);
  }


  /**
   * The hashing algorithm used to hash shingles to signatures (used for
   * strings only).
   */
  public MinHashFactory withHashMethod(HashProvider.HashMethod hashMethod) {
    return new MinHashFactory(k, n, sigSize, hashMethod, exec);
  }


  /**
   * An executor where the kshingling and signature processing tasks are
   * spawned. If nothing is provided then it launches a new executor with
   * the cached thread pool.
   */
  public MinHashFactory withExecutor(ExecutorService executor) {
    return new MinHashFactory(k, n, sigSize, h, executor);
  }


  public double of(String s1, String s2) {
    ExecutorService e = exec;
    boolean usingDefaultExec = false;
    if (e == null || e.isShutdown()) {
      e = Executors.newCachedThreadPool();
      usingDefaultExec = true;
    }
    MinHashStringSimilarity j = new MinHashStringSimilarity(e, sigSize, h, k);
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
    MinHashSetSimilarity j = new MinHashSetSimilarity(e, nAux, sigSize);
    double index = j.calculate(c1, c2);
    if (usingDefaultExec) {
      closeExecutor();
    }
    return index;
  }
}
