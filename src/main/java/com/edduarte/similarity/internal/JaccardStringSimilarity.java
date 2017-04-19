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

package com.edduarte.similarity.internal;

import com.edduarte.similarity.Similarity;
import com.edduarte.similarity.StringSimilarity;
import com.edduarte.similarity.converter.KShingler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class JaccardStringSimilarity implements StringSimilarity {

    private final ExecutorService exec;

    private final KShingler kShingler;


    /**
     * Instantiates a Similarity class for strings using the Jaccard algorithm.
     *
     * @param exec the executor that will receive the concurrent shingle
     *             processing tasks
     * @param k    the length k of the shingles to generate
     */
    public JaccardStringSimilarity(ExecutorService exec, int k) {
        this.exec = exec;
        this.kShingler = new KShingler(k);
    }


    @Override
    public double calculate(String s1, String s2) {
        ShinglePair shingles = getShingles(s1, s2);
        double similarity = Similarity.jaccardIndex(shingles.shingles1, shingles.shingles2);
        shingles = null;
        return similarity;
    }


    ShinglePair getShingles(String s1, String s2) {

        Future<List<CharSequence>> future1 = exec.submit(kShingler.apply(s1));
        Future<List<CharSequence>> future2 = exec.submit(kShingler.apply(s2));

        try {
            List<CharSequence> shingles1 = future1.get();
            List<CharSequence> shingles2 = future2.get();
            return new ShinglePair(shingles1, shingles2);
        } catch (ExecutionException | InterruptedException ex) {
            String m = "There was a problem processing shingles.";
            throw new RuntimeException(m, ex);
        }
    }


    static class ShinglePair {

        final List<CharSequence> shingles1;

        final List<CharSequence> shingles2;


        ShinglePair(List<CharSequence> shingles1, List<CharSequence> shingles2) {
            this.shingles1 = shingles1;
            this.shingles2 = shingles2;
        }
    }
}
