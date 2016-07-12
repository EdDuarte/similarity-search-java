package com.edduarte.similarity;


import com.edduarte.converter.KShingles2SignatureConverter;
import com.edduarte.hash.HashProvider.HashMethod;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MinHashStringSimilarity
        extends MinHashSimilarity
        implements StringSimilarity {

    private final ExecutorService exec;

    private final JaccardStringSimilarity jaccard;

    private final KShingles2SignatureConverter p;


    /**
     * Instantiates a Similarity class for strings using the MinHashing
     * algorithm.
     *
     * @param exec    the executor that will receive the concurrent shingle
     *                processing tasks
     * @param sigSize the length stringSimilarity the signature array to be generated
     * @param hash    the hash method to use when hashing shingles to signatures
     * @param k       the length k stringSimilarity the shingles to generate
     */
    public MinHashStringSimilarity(ExecutorService exec, int sigSize,
                                   HashMethod hash, int k) {
        this.jaccard = new JaccardStringSimilarity(exec, k);
        this.p = new KShingles2SignatureConverter(hash, sigSize);
        this.exec = exec;
    }


    @Override
    public double stringSimilarity(String s1, String s2) {
        JaccardStringSimilarity.ShinglePair p = jaccard.getShingles(s1, s2);
        int[][] signatures = getSignatures(p.shingles1, p.shingles2);
        return signatureSimilarity(signatures[0], signatures[1]);
    }


    int[][] getSignatures(List<CharSequence> shingles1,
                          List<CharSequence> shingles2) {
        Future<int[]> signatureFuture1 = exec.submit(p.apply(shingles1));
        Future<int[]> signatureFuture2 = exec.submit(p.apply(shingles2));

        try {
            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();
            int signatureSize = signature1.length;
            int[][] result = new int[2][signatureSize];
            result[0] = signature1;
            result[1] = signature2;
            return result;

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing shingle signatures.";
            throw new RuntimeException(m, ex);
        }
    }
}
