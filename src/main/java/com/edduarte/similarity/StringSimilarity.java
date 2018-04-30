package com.edduarte.similarity;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public interface StringSimilarity extends Similarity<String> {

  @Override
  double calculate(String t1, String t2);
}
