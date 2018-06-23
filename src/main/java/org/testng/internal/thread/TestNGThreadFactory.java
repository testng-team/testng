package org.testng.internal.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class TestNGThreadFactory implements ThreadFactory {

  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String name;

  public TestNGThreadFactory(String name) {
    this.name = ThreadUtil.THREAD_NAME + "-" + name + "-";
  }

  @Override
  public Thread newThread(Runnable r) {
    return new Thread(r, name + threadNumber.getAndIncrement());
  }
}
