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


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processor class to retrieve shingles of length k.
 *
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
public final class Signature2BandsConverter
    implements Function<int[], Callable<int[]>> {

  private final int b;

  private final int r;


  public Signature2BandsConverter(int b, int r) {
    this.b = b;
    this.r = r;
  }


  @Override
  public Callable<int[]> apply(int[] sig) {
    List<Integer> sigList = Arrays.stream(sig)
        .boxed()
        .collect(Collectors.toList());
    return new BandsCallable(sigList, b, r);
  }


  private static class BandsCallable implements Callable<int[]> {

    private static final int LARGE_PRIME = 433494437;

    private final List<Integer> sig;

    private final int b;

    private final int r;


    private BandsCallable(List<Integer> sig, int b, int r) {
      this.sig = sig;
      this.b = b;
      this.r = r;
    }


    @Override
    public int[] call() throws Exception {
      int sigSize = sig.size();
      int[] res = new int[b];
      int buckets = sigSize / b;

      for (int i = 0; i < sigSize; i++) {
        int band = Math.min(i / buckets, b - 1);
        res[band] = (int) ((res[band] + (long) sig.get(i) * LARGE_PRIME) % r);
      }

      return res;
    }
  }
}
