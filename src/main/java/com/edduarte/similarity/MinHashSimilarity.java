package com.edduarte.similarity;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class MinHashSimilarity {

    public static double signatureSimilarity(int[] signature1, int[] signature2) {
        double similarity = 0;
        int signatureSize = signature1.length;
        for (int i = 0; i < signatureSize; i++) {
            if (signature1[i] == signature2[i]) {
                similarity++;
            }
        }
        return similarity / signatureSize;
    }
}
