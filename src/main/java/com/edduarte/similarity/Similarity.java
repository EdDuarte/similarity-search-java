package com.edduarte.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Similarity<T> extends DoubleSupplier, BooleanSupplier, Supplier<Double> {

  double DEFAULT_CONFIDENCE_THRESHOLD = 0.5;

  static JaccardFactory jaccard() {
    return new JaccardFactory();
  }

  static MinHashFactory minhash() {
    return new MinHashFactory();
  }

  static LSHFactory lsh() {
    return new LSHFactory();
  }

  static double jaccardIndex(int intersectionCount, int unionCount) {
    return (double) intersectionCount / (double) unionCount;
  }

  static double jaccardIndexFromShingles(
      List<CharSequence> shingles1,
      List<CharSequence> shingles2) {
    return jaccardIndexFromR(shinglesToR(shingles1), shinglesToR(shingles2));
  }

  static double jaccardIndexFromR(List<Integer> r1, List<Integer> r2) {
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

  static ArrayList<Integer> shinglesToR(List<CharSequence> shingles) {
    Map<CharSequence, Integer> occurrences = new HashMap<>();
    ArrayList<Integer> r = new ArrayList<>();
    shingles.forEach(s -> {
      Integer position = occurrences.get(s);
      if (position != null) {
        r.set(position, r.get(position) + 1);

      } else {
        occurrences.put(s, occurrences.size());
        r.add(1);
      }
    });
    return r;
  }

  static double signatureIndex(int[] signature1, int[] signature2) {
    double similarity = 0;
    int signatureSize = signature1.length;
    for (int i = 0; i < signatureSize; i++) {
      if (signature1[i] == signature2[i]) {
        similarity++;
      }
    }
    return similarity / signatureSize;
  }

  static boolean isCandidatePair(int[] bands1, int[] bands2) {
    int bandCount = bands1.length;
    for (int b = 0; b < bandCount; b++) {
      if (bands1[b] == bands2[b]) {
        return true;
      }
    }
    return false;
  }

  T getFirst();

  T getSecond();

  @Override
  double getAsDouble();

  @Override
  default boolean getAsBoolean() {
    return getAsDouble() >= DEFAULT_CONFIDENCE_THRESHOLD;
  }

  @Override
  default Double get() {
    return getAsDouble();
  }
}
