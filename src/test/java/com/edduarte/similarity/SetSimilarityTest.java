package com.edduarte.similarity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public class SetSimilarityTest {

    private static ExecutorService executorService;

    private static Collection<Integer> c1;

    private static Collection<Integer> c2;

    private static Collection<Integer> c3;

    private static Collection<Integer> c4;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        int maxThreads = Runtime.getRuntime().availableProcessors() - 1;
        maxThreads = maxThreads > 0 ? maxThreads : 1;
        executorService = Executors.newFixedThreadPool(maxThreads);
        c1 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
        c2 = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
        c3 = Arrays.asList(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        c4 = Arrays.asList(18, 1, 2, 3, 4, 10, 6, 7, 8);
    }

    @AfterClass
    public static void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            String m = "There was a problem executing the processing tasks.";
            throw new RuntimeException(m, ex);
        }
    }


    @Test
    public void jaccardTest() {
        // for jaccard indexes, difference between expected and actual must be
        // exact
        Similarity.JaccardFactory s = Similarity.jaccard()
                .withExecutor(executorService);

        assertEquals(1.0,                s.of(c1, c2), 0);
        assertEquals(0.7272727272727273, s.of(c1, c3), 0);
        assertEquals(0.7272727272727273, s.of(c2, c3), 0);
        assertEquals(0.6363636363636364, s.of(c1, c4), 0);
        assertEquals(0.6363636363636364, s.of(c2, c4), 0);
        assertEquals(0.5833333333333334, s.of(c3, c4), 0);
    }


    @Test
    public void minHashTest() {
        // for min-hash indexes, which generates signatures for universal hashes
        // using random coefficients, we need a looser delta
        Similarity.MinHashFactory s = Similarity.minhash()
                .withSignatureSize(200)
                .withExecutor(executorService);
        assertEquals(1.0,   s.of(c1, c2), 0);
        assertEquals(0.495, s.of(c1, c3), 0.2);
        assertEquals(0.495, s.of(c2, c3), 0.2);
        assertEquals(0.705, s.of(c1, c4), 0.2);
        assertEquals(0.705, s.of(c2, c4), 0.2);
        assertEquals(0.365, s.of(c3, c4), 0.2);
    }


    @Test
    public void lshTest() {
        // for lsh indexes, which determined candidate pairs but produces
        // jaccard indexes, similarity values can be either the exact index from
        // "jaccardTest" or 0.
        Similarity.LSHFactory s = Similarity.lsh();

        // Because the two sets have the exact same elements, LSH bands
        // will be exactly the same, so this test will always assume these are
        // candidate pairs and return the jaccard index.
        assertEquals(1.0, s.of(c1, c2), 0);

        // For the tests below, however,
        double index = s.of(c1, c3);
        try {
            assertEquals(0.7272727272727273, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }

        index = s.of(c1, c4);
        try {
            assertEquals(0.6363636363636364, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }


    }
}
