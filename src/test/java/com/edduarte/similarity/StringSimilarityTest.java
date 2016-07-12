package com.edduarte.similarity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringSimilarityTest {

    private static ExecutorService executorService;

    private static String s1;

    private static String s2;

    private static String s3;

    private static String s4;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        int maxThreads = Runtime.getRuntime().availableProcessors() - 1;
        maxThreads = maxThreads > 0 ? maxThreads : 1;
        executorService = Executors.newFixedThreadPool(maxThreads);

        s1 = "is the of the 100-eyed giant in Greek mythology.";

        // no differences, index should be 1.0 for all algorithms
        s2 = "is the of the 100-eyed giant in Greek mythology.";

        // differences are not negligible
        s3 = "Argus Panoptes is the name of the 100-eyed giant in Norse mythology.";

        // difference is negligible, so for the most part is should NOT be seen
        // as a candidate pair
        s4 = "is the of the 100-eyed giant in Greek mythology .";
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
        Similarity.JaccardSimilarityFactory s = Similarity
                .jaccard()
                .withShingleLength(3)
                .withExecutor(executorService);
        assertEquals(1.0, s.of(s1, s2), 0);
        assertEquals(0.6825396825396826, s.of(s1, s3), 0);
        assertEquals(0.9772727272727273, s.of(s1, s4), 0);
    }


    @Test
    public void minHashTest() {
        // for min-hash indexes, which generates signatures for universal hashes
        // using random coefficients, we need a looser delta
        Similarity.MinHashSimilarityFactory s = Similarity
                .minhash()
                .withShingleLength(3)
                .withSignatureSize(200)
                .withExecutor(executorService);
        assertEquals(1.0, s.of(s1, s2), 0);
        assertEquals(0.535, s.of(s1, s3), 0.2);
        assertEquals(0.925, s.of(s1, s4), 0.2);
    }


    @Test
    public void lshTest() {
        // for lsh indexes, which determined candidate pairs but produces
        // jaccard indexes, similarity values can be either the exact index from
        // "jaccardTest" or 0.
        Similarity.LSHSimilarityFactory s = Similarity
                .lsh()
                .withShingleLength(3)
                .withExecutor(executorService);

        assertEquals(1.0, s.of(s1, s2), 0);
        // because the two sets have the exact same elements, LSH bands will be
        // exactly the same, so this test will always assume these are candidate
        // pairs and return the jaccard index
        // in other words, no need for a try/catch to test if it's 0 instead

        double index = s.of(s1, s3);
        try {
            assertEquals(0.6825396825396826, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }

        index = s.of(s1, s4);
        try {
            assertEquals(0.9772727272727273, index, 0);
        } catch (AssertionError error) {
            // maybe this was not considered to be a candidate pair, so test if
            // it's 0
            assertEquals(0, index, 0);
        }
    }
}
