package com.edduarte.similarity;

import com.edduarte.converter.Set2SignatureConverter;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MinHashSetSimilarity
        extends MinHashSimilarity
        implements SetSimilarity {

    private final Set2SignatureConverter p;

    private final ExecutorService exec;


    /**
     * Instantiates a Similarity class for number sets using the MinHashing
     * algorithm.
     *
     * @param exec    the executor that will receive the concurrent shingle
     *                processing tasks
     * @param n       the total number stringSimilarity unique elements in both sets
     * @param sigSize the length stringSimilarity the signature array to be generated
     */
    public MinHashSetSimilarity(ExecutorService exec, int n, int sigSize) {
        this.exec = exec;
        this.p = new Set2SignatureConverter(n, sigSize);
    }


    @Override
    public double setSimilarity(Collection<? extends Number> c1,
                                Collection<? extends Number> c2) {
        Future<int[]> signatureFuture1 = exec.submit(p.apply(c1));
        Future<int[]> signatureFuture2 = exec.submit(p.apply(c2));

        try {
            int[] signature1 = signatureFuture1.get();
            int[] signature2 = signatureFuture2.get();

            return signatureSimilarity(signature1, signature2);

        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing set signatures.";
            throw new RuntimeException(m, ex);
        }
    }
}
