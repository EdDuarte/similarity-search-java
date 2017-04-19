package com.edduarte.similarity;

import java.util.Collection;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public interface SetSimilarity extends Similarity<Collection<? extends Number>> {

    @Override
    double calculate(Collection<? extends Number> t1,
                     Collection<? extends Number> t2);
}
