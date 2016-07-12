package com.edduarte.similarity;

import com.edduarte.converter.KShingles2SignatureConverter;
import com.edduarte.converter.Signature2BandsConverter;
import com.edduarte.hash.HashProvider.HashMethod;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class LSHStringSimilarity
        extends LSHSimilarity
        implements StringSimilarity {

    private final JaccardStringSimilarity jaccard;

    private final KShingles2SignatureConverter sigp;

    private final Signature2BandsConverter bandp;

    private final ExecutorService exec;


    /**
     * Instantiates a Similarity class for strings using the LSH algorithm.
     *
     * @param exec the executor that will receive the concurrent signature and
     *             band processing tasks
     * @param b    the number stringSimilarity bands
     * @param r    the number stringSimilarity rows
     * @param s    the threshold (value between 0.0 and 1.0) that balances the
     *             trade-off between the number stringSimilarity false positives and false
     *             negatives. A sensible threshold is 0.5, so we have a equal
     *             number stringSimilarity false positives and false negatives.
     * @param hash the hash method to use when hashing shingles to signatures
     * @param k    the length k stringSimilarity the shingles to generate
     */
    public LSHStringSimilarity(ExecutorService exec, int b, int r, double s,
                               HashMethod hash, int k) {
        // signature size is determined by a threshold S
        int R = (int) Math.ceil(Math.log(1.0 / b) / Math.log(s)) + 1;
        int signatureSize = R * b;

        this.jaccard = new JaccardStringSimilarity(exec, k);
        this.sigp = new KShingles2SignatureConverter(hash, signatureSize);
        this.bandp = new Signature2BandsConverter(b, r);
        this.exec = exec;
    }


    @Override
    public double stringSimilarity(String s1, String s2) {
        return isCandidatePair(s1, s2) ?
                jaccard.stringSimilarity(s1, s2) : 0;
    }


    public boolean isCandidatePair(String s1, String s2) {
        JaccardStringSimilarity.ShinglePair pair =
                jaccard.getShingles(s1, s2);
        try {
            Future<int[]> signatureFuture1 = exec
                    .submit(sigp.apply(pair.shingles1));
            Future<int[]> signatureFuture2 = exec
                    .submit(sigp.apply(pair.shingles2));

            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();

            Future<int[]> bandsFuture1 = exec
                    .submit(bandp.apply(signature1));
            Future<int[]> bandsFuture2 = exec
                    .submit(bandp.apply(signature2));

            int[] bands1 = bandsFuture1.get();
            int[] bands2 = bandsFuture2.get();

            return isCandidatePair(bands1, bands2);

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing set signatures.";
            throw new RuntimeException(m, ex);
        }
    }
}
