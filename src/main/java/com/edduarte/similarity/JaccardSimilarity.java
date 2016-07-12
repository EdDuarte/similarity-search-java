package com.edduarte.similarity;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class JaccardSimilarity {

    public static double coefficient(int intersectionCount, int unionCount) {
        return (double) intersectionCount / (double) unionCount;
    }
}
