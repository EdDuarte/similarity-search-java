package com.edduarte.similarity;

import com.edduarte.converter.Set2SignatureConverter;
import com.edduarte.converter.Signature2BandsConverter;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class LSHSetSimilarity
        extends LSHSimilarity
        implements SetSimilarity {

    private final JaccardSetSimilarity jaccard;

    private final Set2SignatureConverter sigp;

    private final Signature2BandsConverter bandp;

    private final ExecutorService exec;


    /**
     * Instantiates a Similarity class for number sets using the LSH algorithm.
     *
     * @param exec the executor that will receive the concurrent signature and
     *             band processing tasks
     * @param n    the total number stringSimilarity unique elements in both sets
     * @param b    the number stringSimilarity bands
     * @param r    the number stringSimilarity rows
     * @param s    the threshold (value between 0.0 and 1.0) that balances the
     *             trade-off between the number stringSimilarity false positives and false
     *             negatives. A sensible threshold is 0.5, so we have a equal
     *             number stringSimilarity false positives and false negatives.
     */
    public LSHSetSimilarity(ExecutorService exec, int n, int b, int r, double s) {
        // signature size is determined by a threshold S
        this.exec = exec;
        int R = (int) Math.ceil(Math.log(1.0 / b) / Math.log(s)) + 1;
        int sigSize = R * b;
        this.jaccard = new JaccardSetSimilarity();
        this.sigp = new Set2SignatureConverter(n, sigSize);
        this.bandp = new Signature2BandsConverter(b, r);
    }


    @Override
    public double setSimilarity(Collection<? extends Number> c1,
                                Collection<? extends Number> c2) {
        return isCandidatePair(c1, c2) ?
                jaccard.setSimilarity(c1, c2) : 0;
    }


    public boolean isCandidatePair(Collection<? extends Number> c1,
                                   Collection<? extends Number> c2) {
        try {
            Future<int[]> signatureFuture1 = exec.submit(sigp.apply(c1));
            Future<int[]> signatureFuture2 = exec.submit(sigp.apply(c2));

            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();

            Future<int[]> bandsFuture1 = exec.submit(bandp.apply(signature1));
            Future<int[]> bandsFuture2 = exec.submit(bandp.apply(signature2));

            int[] bands1 = bandsFuture1.get();
            int[] bands2 = bandsFuture2.get();

            return isCandidatePair(bands1, bands2);

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing set signatures.";
            throw new RuntimeException(m, ex);
        }
    }
}
