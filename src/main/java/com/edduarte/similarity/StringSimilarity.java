package com.edduarte.similarity;

import java.util.Objects;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class StringSimilarity implements Similarity<String> {

  private final String s1;

  private final String s2;


  protected StringSimilarity(String s1, String s2) {
    Objects.requireNonNull(s1, "Strings to compare must not be null");
    Objects.requireNonNull(s2, "Strings to compare must not be null");
    this.s1 = s1;
    this.s2 = s2;
  }


  @Override
  public String getFirst() {
    return s1;
  }


  @Override
  public String getSecond() {
    return s2;
  }


  @Override
  public double getAsDouble() {
    return s1.equals(s2) ? 1 : 0;
  }
}
