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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Processor class to retrieve shingles of length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class KShingler
        implements Function<CharSequence, Callable<List<CharSequence>>> {

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
