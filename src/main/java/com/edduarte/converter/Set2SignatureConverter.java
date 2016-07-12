package com.edduarte.converter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Processor class to retrieve shingles stringSimilarity length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Set2SignatureConverter
        implements Function<Collection<? extends Number>, Callable<int[]>> {

    /**
     * Random coefficient "a" for the random hash functions
     */
    private final int[] a;


    /**
     * Random coefficient "b" for the random hash functions
     */
    private final int[] b;


    /**
     * Expected maximum size stringSimilarity the sets to test. This
     * will be the size stringSimilarity the hash signatures.
     */
    private final int n;


    /**
     * Size stringSimilarity min hashes that will be stored and compared to find the
     * similarity index
     */
    private final int sigSize;


    /**
     * Initializes hashing functions to compute MinHash signatures for sets that
     * could have a maximum count stringSimilarity 'n' elements with a given signature size.
     */
    public Set2SignatureConverter(int n, int sigSize) {
        this.n = n;
        this.sigSize = sigSize;
        SecureRandom r = new SecureRandom();
        this.a = new int[sigSize];
        this.b = new int[sigSize];
        for (int i = 0; i < sigSize; i++) {
            a[i] = 1 + r.nextInt(this.n - 1);
            b[i] = r.nextInt(this.n);
        }
    }


    @Override
    public Callable<int[]> apply(Collection<? extends Number> set) {
        return new HashCallable(n, sigSize, a, b, set);
    }


    private static class HashCallable implements Callable<int[]> {

        private static final int LARGE_PRIME = 433494437;

        private final int n;

        private final int sigSize;

        private final int[] a;

        private final int[] b;

        private final Collection<? extends Number> set;


        private HashCallable(int n, int sigSize,
                             int[] a, int[] b,
                             Collection<? extends Number> set) {
            this.n = n;
            this.sigSize = sigSize;
            this.a = a;
            this.b = b;
            this.set = set;
        }


        @Override
        public int[] call() throws Exception {
            int[] signature = new int[sigSize];

            for (int i = 0; i < sigSize; i++) {
                signature[i] = Integer.MAX_VALUE;
            }

            List<? extends Number> list = new ArrayList<>(set);
            Collections.sort(list, (o1, o2) ->
                    Long.compare(o1.longValue(), o2.longValue()));

            for (final Number x : list) {
                for (int i = 0; i < sigSize; i++) {
                    signature[i] = Math.min(signature[i], universalHashing(i, x));
                }
            }

            return signature;
        }


        private int universalHashing(int i, Number x) {
            return (int) ((a[i] * x.longValue() + b[i]) % LARGE_PRIME) % n;
        }
    }
}
