package com.edduarte.similarity;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class SetSimilarity implements Similarity<Collection<? extends Number>> {

  private final Collection<? extends Number> c1;

  private final Collection<? extends Number> c2;


  protected SetSimilarity(Collection<? extends Number> c1, Collection<? extends Number> c2) {
    Objects.requireNonNull(c1, "Sets to compare must not be null");
    Objects.requireNonNull(c2, "Sets to compare must not be null");
    this.c1 = c1;
    this.c2 = c2;
  }


  @Override
  public Collection<? extends Number> getFirst() {
    return c1;
  }


  @Override
  public Collection<? extends Number> getSecond() {
    return c2;
  }


  @Override
  public double getAsDouble() {
    return c1.equals(c2) ? 1 : 0;
  }
}
