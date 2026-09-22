package org.testng.concurrency.samples;

import org.testng.annotations.Test;

public class Sample1 extends BaseThreadTest {

  @Test
  public void s1() {
    logThread(Thread.currentThread().getId());
  }
}
