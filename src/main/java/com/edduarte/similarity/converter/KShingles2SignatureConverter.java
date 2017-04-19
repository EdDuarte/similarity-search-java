/*
 * Copyright 2017 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.similarity.converter;

import com.edduarte.similarity.hash.HashProvider.HashMethod;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processor class to convert shingles to hash signatures.
 *
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
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
