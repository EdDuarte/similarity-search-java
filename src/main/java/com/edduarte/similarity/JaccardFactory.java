package com.edduarte.similarity;

import com.edduarte.similarity.internal.JaccardSetSimilarity;
import com.edduarte.similarity.internal.JaccardStringSimilarity;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public final class JaccardFactory extends Factory {

  private int k;


  JaccardFactory() {
    super();
    // sensible defaults for common small strings (smaller than an email)
    // or small collections (between 10 to 40 elements)
    this.k = 2;
  }


  /**
   * Length of n-gram shingles that are used for comparison (used for
   * strings only).
   */
  public synchronized JaccardFactory withShingleLength(int shingleLength) {
    this.k = shingleLength;
    return this;
  }


  /**
   * An executor where the kshingling tasks are spawned. If nothing is
   * provided then it launches a new executor with the cached thread pool.
   */
  public synchronized JaccardFactory withExecutor(ExecutorService executor) {
    setExec(executor);
    return this;
  }


  @Override
  StringSimilarity initStringSimilarityTask(
      String s1, String s2, ExecutorService exec) {
    return new JaccardStringSimilarity(s1, s2, k, exec);
  }


  @Override
  SetSimilarity initSetSimilarityTask(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2,
      ExecutorService exec) {
    return new JaccardSetSimilarity(c1, c2);
  }
}
