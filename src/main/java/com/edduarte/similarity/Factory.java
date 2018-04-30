package com.edduarte.similarity;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
abstract class Factory {

  private ExecutorService exec;


  Factory() {
    this.exec = null;
  }


  void setExec(ExecutorService exec) {
    this.exec = exec;
  }


  abstract StringSimilarity initStringSimilarityTask(
      String s1, String s2, ExecutorService exec);


  abstract SetSimilarity initSetSimilarityTask(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2,
      ExecutorService exec);


  public synchronized final double of(String s1, String s2) {
    StringSimilarity task;
    if (exec != null && !exec.isShutdown()) {
      task = initStringSimilarityTask(s1, s2, exec);
    } else {
      ForkJoinPool e = ForkJoinPool.commonPool();
      task = initStringSimilarityTask(s1, s2, e);
    }
    return task.getAsDouble();
  }


  public synchronized final CompletableFuture<Double> ofAsync(
      String s1,
      String s2) {
    if (exec != null && !exec.isShutdown()) {
      StringSimilarity task = initStringSimilarityTask(s1, s2, exec);
      return CompletableFuture.supplyAsync(task, exec);
    } else {
      ForkJoinPool e = ForkJoinPool.commonPool();
      StringSimilarity task = initStringSimilarityTask(s1, s2, e);
      return CompletableFuture.supplyAsync(task);
    }
  }


  public synchronized final double of(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    SetSimilarity task;
    if (exec != null && !exec.isShutdown()) {
      task = initSetSimilarityTask(c1, c2, exec);
    } else {
      ForkJoinPool e = ForkJoinPool.commonPool();
      task = initSetSimilarityTask(c1, c2, e);
    }
    return task.getAsDouble();
  }


  public synchronized final CompletableFuture<Double> ofAsync(
      Collection<? extends Number> c1,
      Collection<? extends Number> c2) {
    if (exec != null && !exec.isShutdown()) {
      SetSimilarity task = initSetSimilarityTask(c1, c2, exec);
      return CompletableFuture.supplyAsync(task, exec);
    } else {
      ForkJoinPool e = ForkJoinPool.commonPool();
      SetSimilarity task = initSetSimilarityTask(c1, c2, e);
      return CompletableFuture.supplyAsync(task);
    }
  }
}
