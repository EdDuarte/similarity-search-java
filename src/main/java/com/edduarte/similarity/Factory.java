package com.edduarte.similarity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Eduardo Duarte (<a href="mailto:hi@edduarte.com">hi@edduarte.com</a>)
 * @version 0.0.1
 * @since 0.0.1
 */
class Factory {

  final ExecutorService exec;


  Factory() {
    this.exec = null;
  }


  Factory(ExecutorService executor) {
    this.exec = executor;
  }


  void closeExecutor() {
    if (exec == null) {
      return;
    }
    exec.shutdown();
    try {
      exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    } catch (InterruptedException ex) {
      String m = "There was a problem executing the processing tasks.";
      throw new RuntimeException(m, ex);
    }
  }
}
