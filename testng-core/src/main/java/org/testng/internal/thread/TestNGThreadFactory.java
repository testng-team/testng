package org.testng.internal.thread;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestNGThreadFactory implements ThreadFactory {

  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String name;
  private final Set<Thread> threads = Collections.newSetFromMap(new WeakHashMap<>());

  public TestNGThreadFactory(String name) {
    this.name = ThreadUtil.THREAD_NAME + "-" + name + "-";
  }

  public Collection<Thread> getRunningThreads() {
    return threads.stream().filter(Thread::isAlive).collect(Collectors.toList());
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = new Thread(r, name + threadNumber.getAndIncrement());
    threads.add(thread);
    return thread;
  }
}
