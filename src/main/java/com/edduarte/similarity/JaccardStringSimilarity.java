package com.edduarte.similarity;

import com.edduarte.converter.KShingler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class JaccardStringSimilarity
        extends JaccardSimilarity
        implements StringSimilarity {

    private final ExecutorService exec;

    private final KShingler kShingler;


    /**
     * Instantiates a Similarity class for strings using the Jaccard algorithm.
     *
     * @param exec the executor that will receive the concurrent shingle
     *             processing tasks
     * @param k    the length k stringSimilarity the shingles to generate
     */
    public JaccardStringSimilarity(ExecutorService exec, int k) {
        this.exec = exec;
        this.kShingler = new KShingler(k);
    }


    public static double shingleSimilarity(List<CharSequence> shingles1,
                                           List<CharSequence> shingles2) {

        ArrayList<Integer> r1 = new ArrayList<>();
        ArrayList<Integer> r2 = new ArrayList<>();

        Map<CharSequence, Integer> shingleOccurrencesMap1 = new HashMap<>();
        shingles1.stream().forEach(s -> {
            if (shingleOccurrencesMap1.containsKey(s)) {
                int position = shingleOccurrencesMap1.get(s);
                r1.set(position, r1.get(position) + 1);

            } else {
                shingleOccurrencesMap1.put(s, shingleOccurrencesMap1.size());
                r1.add(1);
            }
        });

        Map<CharSequence, Integer> shingleOccurrencesMap2 = new HashMap<>();
        shingles2.stream().forEach(s -> {
            if (shingleOccurrencesMap2.containsKey(s)) {
                int position = shingleOccurrencesMap2.get(s);
                r2.set(position, r2.get(position) + 1);

            } else {
                shingleOccurrencesMap2.put(s, shingleOccurrencesMap2.size());
                r2.add(1);
            }
        });

        int maxLength = Math.max(r1.size(), r2.size());

        int intersection = 0;
        int union = 0;

        for (int i = 0; i < maxLength; i++) {
            int value1 = i < r1.size() ? r1.get(i) : 0;
            int value2 = i < r2.size() ? r2.get(i) : 0;
            if (value1 > 0 || value2 > 0) {
                union++;

                if (value1 > 0 && value2 > 0) {
                    intersection++;
                }
            }
        }

        return coefficient(intersection, union);
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


    @Override
    public double stringSimilarity(String s1, String s2) {
        ShinglePair shingles = getShingles(s1, s2);
        return shingleSimilarity(shingles.shingles1, shingles.shingles2);
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
