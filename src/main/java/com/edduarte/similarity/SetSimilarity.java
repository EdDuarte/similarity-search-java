package com.edduarte.similarity;

import java.util.Collection;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SetSimilarity {

    double setSimilarity(Collection<? extends Number> c1,
                         Collection<? extends Number> c2);
}
