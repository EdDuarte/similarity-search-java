package com.edduarte.similarity;

import com.edduarte.similarity.hash.HashProvider.HashMethod;
import com.edduarte.similarity.internal.JaccardSetSimilarity;
import com.edduarte.similarity.internal.JaccardStringSimilarity;
import com.edduarte.similarity.internal.LSHSetSimilarity;
import com.edduarte.similarity.internal.LSHStringSimilarity;
import com.edduarte.similarity.internal.MinHashSetSimilarity;
import com.edduarte.similarity.internal.MinHashStringSimilarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Similarity<T> {

    double calculate(T t1, T t2);


    public static JaccardFactory jaccard() {
        return JaccardFactory.SINGLETON;
    }

    public static MinHashFactory minhash() {
        return MinHashFactory.SINGLETON;
    }

    public static LSHFactory lsh() {
        return LSHFactory.SINGLETON;
    }


    public static double jaccardIndex(int intersectionCount, int unionCount) {
        return (double) intersectionCount / (double) unionCount;
    }


    public static double jaccardIndex(List<CharSequence> shingles1,
                                      List<CharSequence> shingles2) {

        ArrayList<Integer> r1 = new ArrayList<>();
        ArrayList<Integer> r2 = new ArrayList<>();

        Map<CharSequence, Integer> shingleOccurrencesMap1 = new HashMap<>();
        shingles1.forEach(s -> {
            if (shingleOccurrencesMap1.containsKey(s)) {
                int position = shingleOccurrencesMap1.get(s);
                r1.set(position, r1.get(position) + 1);

            } else {
                shingleOccurrencesMap1.put(s, shingleOccurrencesMap1.size());
                r1.add(1);
            }
        });

        Map<CharSequence, Integer> shingleOccurrencesMap2 = new HashMap<>();
        shingles2.forEach(s -> {
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

        return jaccardIndex(intersection, union);
    }


    public static double signatureIndex(int[] signature1, int[] signature2) {
        double similarity = 0;
        int signatureSize = signature1.length;
        for (int i = 0; i < signatureSize; i++) {
            if (signature1[i] == signature2[i]) {
                similarity++;
            }
        }
        return similarity / signatureSize;
    }


    public static boolean isCandidatePair(int[] bands1, int[] bands2) {
        int bandCount = bands1.length;
        for (int b = 0; b < bandCount; b++) {
            if (bands1[b] == bands2[b]) {
                return true;
            }
        }
        return false;
    }


    static void closeExecutor(ExecutorService exec) {
        exec.shutdown();
        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            String m = "There was a problem executing the processing tasks.";
            throw new RuntimeException(m, ex);
        }
    }


    public static final class JaccardFactory {

        private static final JaccardFactory SINGLETON = new JaccardFactory();

        // sensible defaults for common small strings (smaller than an email)
        // or small collections (between 10 to 40 elements)
        private int k;

        private ExecutorService exec;


        private JaccardFactory() {
            this.k = 2;
        }


        /**
         * Length of n-gram shingles that are used for comparison (used for
         * strings only).
         */
        public synchronized JaccardFactory withShingleLength(int shingleLength) {
            this.k = shingleLength;
            return this;
        }


        /**
         * An executor where the kshingling tasks are spawned. If nothing is
         * provided then it launches a new executor with the cached thread pool.
         */
        public synchronized JaccardFactory withExecutor(ExecutorService executor) {
            this.exec = executor;
            return this;
        }


        public synchronized double of(String s1, String s2) {
            ExecutorService e = exec;
            boolean usingDefaultExec = false;
            if (e == null || e.isShutdown()) {
                e = Executors.newCachedThreadPool();
                usingDefaultExec = true;
            }
            JaccardStringSimilarity j = new JaccardStringSimilarity(e, k);
            double index = j.calculate(s1, s2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }


        public synchronized double of(Collection<? extends Number> c1,
                                      Collection<? extends Number> c2) {
            JaccardSetSimilarity j = new JaccardSetSimilarity();
            return j.calculate(c1, c2);
        }
    }


    public static final class MinHashFactory {

        private static final MinHashFactory SINGLETON = new MinHashFactory();

        // sensible defaults for common small strings (smaller than an email)
        // or small collections (between 10 to 40 elements)
        private int k;

        private int n;

        private int sigSize;

        private HashMethod h;

        private ExecutorService exec;

        private MinHashFactory() {
            this.k = 2;
            this.n = -1;
            this.sigSize = 100;
            this.h = HashMethod.Murmur3;
        }


        /**
         * Length of n-gram shingles that are used for comparison (used for
         * strings only).
         */
        public synchronized MinHashFactory withShingleLength(int shingleLength) {
            this.k = shingleLength;
            return this;
        }


        /**
         * Number of unique elements in both sets (used for sets only). For
         * example, if set1=[4, 5, 6, 7, 8] and set2=[7, 8, 9, 10], this value
         * should be 7. If nothing is provided, this value is determined in
         * pre-processing.
         */
        public synchronized MinHashFactory withNumberOfElements(int elementCount) {
            this.n = elementCount;
            return this;
        }


        /**
         * The size of the generated signatures, which are compared to determine
         * similarity.
         */
        public synchronized MinHashFactory withSignatureSize(int signatureSize) {
            this.sigSize = signatureSize;
            return this;
        }


        /**
         * The hashing algorithm used to hash shingles to signatures (used for
         * strings only).
         */
        public synchronized MinHashFactory withHashMethod(HashMethod hashMethod) {
            this.h = hashMethod;
            return this;
        }


        /**
         * An executor where the kshingling and signature processing tasks are
         * spawned. If nothing is provided then it launches a new executor with
         * the cached thread pool.
         */
        public synchronized MinHashFactory withExecutor(ExecutorService executor) {
            this.exec = executor;
            return this;
        }


        public synchronized double of(String s1, String s2) {
            ExecutorService e = exec;
            boolean usingDefaultExec = false;
            if (e == null || e.isShutdown()) {
                e = Executors.newCachedThreadPool();
                usingDefaultExec = true;
            }
            MinHashStringSimilarity j = new MinHashStringSimilarity(
                    e, sigSize, h, k);
            double index = j.calculate(s1, s2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }


        public synchronized double of(Collection<? extends Number> c1,
                                      Collection<? extends Number> c2) {
            ExecutorService e = exec;
            boolean usingDefaultExec = false;
            if (e == null || e.isShutdown()) {
                e = Executors.newCachedThreadPool();
                usingDefaultExec = true;
            }
            int nAux = n;
            if (nAux < 0) {
                Set<Number> unionSet = new HashSet<>(c1);
                unionSet.addAll(c2);
                nAux = (int) unionSet.parallelStream().distinct().count();
            }
            MinHashSetSimilarity j = new MinHashSetSimilarity(e, nAux, sigSize);
            double index = j.calculate(c1, c2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }
    }


    public static final class LSHFactory {

        private static final LSHFactory SINGLETON = new LSHFactory();

        // sensible defaults for common small strings (smaller than an email)
        // or small collections (between 10 to 40 elements)
        private int k;

        private int n;

        private int b;

        private int r;

        private double s;

        private HashMethod h;

        private ExecutorService exec;


        private LSHFactory() {
            this.k = 2;
            this.n = -1;
            this.b = 20;
            this.r = 5;
            this.s = 0.5;
            this.h = HashMethod.Murmur3;
        }


        /**
         * Length of n-gram shingles that are used when generating signatures
         * (used for strings only).
         */
        public synchronized LSHFactory withShingleLength(int shingleLength) {
            this.k = shingleLength;
            return this;
        }


        /**
         * Number of unique elements in both sets (used for sets only). For
         * example, if set1=[4, 5, 6, 7, 8] and set2=[7, 8, 9, 10], this value
         * should be 7. If nothing is provided, this value is determined in
         * pre-processing.
         */
        public synchronized LSHFactory withNumberOfElements(int elementCount) {
            this.n = elementCount;
            return this;
        }


        /**
         * The number of bands where the minhash signatures will be structured.
         */
        public synchronized LSHFactory withNumberOfBands(int bandCount) {
            this.b = bandCount;
            return this;
        }


        /**
         * The number of rows where the minhash signatures will be structured.
         */
        public synchronized LSHFactory withNumberOfRows(int rowCount) {
            this.r = rowCount;
            return this;
        }


        /**
         * A threshold S that balances the number of false positives and false
         * negatives.
         */
        public synchronized LSHFactory withThreshold(double threshold) {
            this.s = threshold;
            return this;
        }


        /**
         * The hashing algorithm used to hash shingles to signatures (used for
         * strings only).
         */
        public synchronized LSHFactory withHashMethod(HashMethod hashMethod) {
            this.h = hashMethod;
            return this;
        }


        /**
         * An executor where the kshingling and signature processing tasks are
         * spawned. If nothing is provided then it launches a new executor with
         * the cached thread pool.
         */
        public synchronized LSHFactory withExecutor(ExecutorService executor) {
            this.exec = executor;
            return this;
        }


        public synchronized double of(String s1, String s2) {
            ExecutorService e = exec;
            boolean usingDefaultExec = false;
            if (e == null || e.isShutdown()) {
                e = Executors.newCachedThreadPool();
                usingDefaultExec = true;
            }
            LSHStringSimilarity j = new LSHStringSimilarity(e, b, r, s, h, k);
            double index = j.calculate(s1, s2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }


        public synchronized double of(Collection<? extends Number> c1,
                                      Collection<? extends Number> c2) {
            ExecutorService e = exec;
            boolean usingDefaultExec = false;
            if (e == null || e.isShutdown()) {
                e = Executors.newCachedThreadPool();
                usingDefaultExec = true;
            }
            int nAux = n;
            if (nAux < 0) {
                Set<Number> unionSet = new HashSet<>(c1);
                unionSet.addAll(c2);
                nAux = (int) unionSet.parallelStream().distinct().count();
            }
            LSHSetSimilarity j = new LSHSetSimilarity(e, nAux, b, r, s);
            double index = j.calculate(c1, c2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }
    }
}
