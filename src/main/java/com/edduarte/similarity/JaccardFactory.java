package com.edduarte.similarity;

import com.edduarte.similarity.internal.JaccardSetSimilarity;
import com.edduarte.similarity.internal.JaccardStringSimilarity;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public final class JaccardFactory extends Factory {

  static final JaccardFactory SINGLETON = new JaccardFactory();

  private final int k;


  private JaccardFactory() {
    super();
    // sensible defaults for common small strings (smaller than an email)
    // or small collections (between 10 to 40 elements)
    this.k = 2;
  }


  private JaccardFactory(int k, ExecutorService exec) {
    super(exec);
    this.k = k;
  }


  public JaccardFactory with(int shingleLength, ExecutorService executor) {
    return new JaccardFactory(shingleLength, executor);
  }


  /**
   * Length of n-gram shingles that are used for comparison (used for
   * strings only).
   */
  public JaccardFactory withShingleLength(int shingleLength) {
    return new JaccardFactory(shingleLength, exec);
  }


  /**
   * An executor where the kshingling tasks are spawned. If nothing is
   * provided then it launches a new executor with the cached thread pool.
   */
  public JaccardFactory withExecutor(ExecutorService executor) {
    return new JaccardFactory(k, executor);
  }


  public double of(String s1, String s2) {
    ExecutorService e = exec;
    boolean usingDefaultExec = false;
    if (e == null || e.isShutdown()) {
      e = Executors.newCachedThreadPool();
      usingDefaultExec = true;
    }
    JaccardStringSimilarity j = new JaccardStringSimilarity(e, k);
    double index = j.calculate(s1, s2);
    if (usingDefaultExec) {
      closeExecutor();
    }
    return index;
  }


  public double of(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    JaccardSetSimilarity j = new JaccardSetSimilarity();
    return j.calculate(c1, c2);
  }
}
