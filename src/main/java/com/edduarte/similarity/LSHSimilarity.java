package com.edduarte.similarity;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class LSHSimilarity {

    public static boolean isCandidatePair(int[] bands1, int[] bands2) {
//        int similarBands = 0;
        int bandCount = bands1.length;
        for (int b = 0; b < bandCount; b++) {
            if (bands1[b] == bands2[b]) {
                return true;
            }
        }
        return false;
    }
}
