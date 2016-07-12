package com.edduarte.similarity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class JaccardSetSimilarity
        extends JaccardSimilarity
        implements SetSimilarity {


    /**
     * Instantiates a Similarity class for number sets using the Jaccard
     * algorithm.
     */
    public JaccardSetSimilarity() {
    }


    public double setSimilarity(Collection<? extends Number> c1,
                                Collection<? extends Number> c2) {
        Set<Number> intersectionSet = new HashSet<>();
        for (Number number : c1) {
            if (c2.contains(number)) {
                intersectionSet.add(number);
            }
        }
        Set<Number> unionSet = new HashSet<>(c1);
        unionSet.addAll(c2);
        return coefficient(intersectionSet.size(), unionSet.size());
    }
}
