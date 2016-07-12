package com.edduarte.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Processor class to retrieve shingles stringSimilarity length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class KShingler implements Function<CharSequence, Callable<List<CharSequence>>> {

    /**
     * K value, generating shingles with length k
     */
    private final int k;

    private final Predicate<CharSequence> stopper;


    public KShingler(int k) {
        this(k, null);
    }


    public KShingler(int k, Predicate<CharSequence> stopper) {
        this.k = k;
        this.stopper = stopper;
    }


    @Override
    public Callable<List<CharSequence>> apply(CharSequence s) {
        return new ShingleCallable(k, s, stopper);
    }


    private static class ShingleCallable implements Callable<List<CharSequence>> {

        private final int k;

        private final CharSequence text;

        private final Predicate<CharSequence> stopper;


        private ShingleCallable(int k, CharSequence text,
                                Predicate<CharSequence> stopper) {
            this.k = k;
            this.text = text;
            this.stopper = stopper;
        }


        @Override
        public List<CharSequence> call() {

            List<CharSequence> shingles = new ArrayList<>();

            for (int i = 0; i < (text.length() - k + 1); i++) {
                CharSequence s = text.subSequence(i, i + k);

                if (stopper != null && stopper.test(s)) {
                    // shingle matches a stopword, so skip it
                    continue;
                }

                shingles.add(s);
            }

            return shingles;
        }
    }
}
