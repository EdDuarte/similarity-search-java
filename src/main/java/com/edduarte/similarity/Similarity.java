package com.edduarte.similarity;

import com.edduarte.hash.HashProvider.HashMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Similarity {

    public static JaccardSimilarityFactory jaccard() {
        return new JaccardSimilarityFactory();
    }

    public static MinHashSimilarityFactory minhash() {
        return new MinHashSimilarityFactory();
    }

    public static LSHSimilarityFactory lsh() {
        return new LSHSimilarityFactory();
    }

    public static final class JaccardSimilarityFactory {

        // sensible defaults for common small strings (smaller than an email)
        // or small collections (between 10 to 40 elements)
        private int k = 2;

        private ExecutorService exec = null;


        private static void closeExecutor(ExecutorService exec) {
            exec.shutdown();
            try {
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                String m = "There was a problem executing the processing tasks.";
                throw new RuntimeException(m, ex);
            }
        }


        public JaccardSimilarityFactory withShingleLength(int shingleLength) {
            this.k = shingleLength;
            return this;
        }


        public JaccardSimilarityFactory withExecutor(ExecutorService executor) {
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
            double index = j.stringSimilarity(s1, s2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }


        public synchronized double of(Collection<? extends Number> c1,
                                      Collection<? extends Number> c2) {
            JaccardSetSimilarity j = new JaccardSetSimilarity();
            return j.setSimilarity(c1, c2);
        }
    }


    public static final class MinHashSimilarityFactory {

        // sensible defaults for common small strings (smaller than an email)
        // or small collections (between 10 to 40 elements)
        private int k = 2;

        private int n = -1;

        private int sigSize = 100;

        private HashMethod h = HashMethod.Murmur3;

        private ExecutorService exec = null;


        private static void closeExecutor(ExecutorService exec) {
            exec.shutdown();
            try {
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                String m = "There was a problem executing the processing tasks.";
                throw new RuntimeException(m, ex);
            }
        }


        public MinHashSimilarityFactory withShingleLength(int shingleLength) {
            this.k = shingleLength;
            return this;
        }


        public MinHashSimilarityFactory withNumberOfElements(int elementCount) {
            this.n = elementCount;
            return this;
        }


        public MinHashSimilarityFactory withSignatureSize(int signatureSize) {
            this.sigSize = signatureSize;
            return this;
        }


        public MinHashSimilarityFactory withHashMethod(HashMethod hashMethod) {
            this.h = hashMethod;
            return this;
        }


        public MinHashSimilarityFactory withExecutor(ExecutorService executor) {
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
            double index = j.stringSimilarity(s1, s2);
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
            double index = j.setSimilarity(c1, c2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }
    }


    public static final class LSHSimilarityFactory {

        // sensible defaults for common small strings (smaller than an email)
        // or small collections (between 10 to 40 elements)
        private int k = 2;

        private int n = -1;

        private int b = 20;

        private int r = 5;

        private double s = 0.5;

        private HashMethod h = HashMethod.Murmur3;

        private ExecutorService exec = null;


        private static void closeExecutor(ExecutorService exec) {
            exec.shutdown();
            try {
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                String m = "There was a problem executing the processing tasks.";
                throw new RuntimeException(m, ex);
            }
        }


        public LSHSimilarityFactory withShingleLength(int shingleLength) {
            this.k = shingleLength;
            return this;
        }


        public LSHSimilarityFactory withNumberOfElements(int elementCount) {
            this.n = elementCount;
            return this;
        }


        public LSHSimilarityFactory withNumberOfBands(int bandCount) {
            this.b = bandCount;
            return this;
        }


        public LSHSimilarityFactory withNumberOfRows(int rowCount) {
            this.r = rowCount;
            return this;
        }


        public LSHSimilarityFactory withThreshold(int threshold) {
            this.s = threshold;
            return this;
        }


        public LSHSimilarityFactory withHashMethod(HashMethod hashMethod) {
            this.h = hashMethod;
            return this;
        }


        public LSHSimilarityFactory withExecutor(ExecutorService executor) {
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
            double index = j.stringSimilarity(s1, s2);
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
            double index = j.setSimilarity(c1, c2);
            if (usingDefaultExec) {
                closeExecutor(e);
            }
            return index;
        }
    }
}
