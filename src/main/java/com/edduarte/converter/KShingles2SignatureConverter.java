package com.edduarte.converter;

import com.edduarte.hash.HashProvider.HashMethod;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processor class to convert shingles to hash signatures.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class KShingles2SignatureConverter
        implements Function<List<CharSequence>, Callable<int[]>> {

    private final HashMethod hash;

    private final int sigSize;


    public KShingles2SignatureConverter(HashMethod hash, int sigSize) {
        this.hash = hash;
        this.sigSize = sigSize;
    }


    @Override
    public Callable<int[]> apply(List<CharSequence> shingles) {
        return new SignatureCallable(shingles, hash, sigSize);
    }


    private class SignatureCallable implements Callable<int[]> {

        private final List<CharSequence> shingles;

        private final HashMethod hash;

        private final int sigSize;


        private SignatureCallable(List<CharSequence> shingles,
                                  HashMethod hash,
                                  int sigSize) {
            this.shingles = shingles;
            this.hash = hash;
            this.sigSize = sigSize;
        }


        @Override
        public int[] call() {
            int[] sig = new int[sigSize];

            for (int i = 0; i < sigSize; i++) {
                sig[i] = Integer.MAX_VALUE;
            }

            List<String> aux = shingles.parallelStream()
                    .map(CharSequence::toString)
                    .collect(Collectors.toList());

            Collections.sort(aux);

            for (final String s : aux) {
                byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
                int[] hash = this.hash.getHashFunction()
                        .hash(bytes, Integer.MAX_VALUE, sigSize);
                for (int i = 0; i < sigSize; i++) {
                    sig[i] = Math.min(sig[i], hash[i]);
                }
            }

            return sig;
        }
    }
}
