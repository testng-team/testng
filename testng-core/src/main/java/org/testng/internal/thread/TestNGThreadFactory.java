package org.testng.internal.thread;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestNGThreadFactory implements ThreadFactory {

  // TestNG builds a new factory for each timed invocation, and a timed-out thread can keep running
  // beside the next one. Factories with the same name share one counter, so the two threads get
  // different names (GITHUB-2091).
  private static final ConcurrentMap<String, AtomicInteger> THREAD_NUMBERS =
      new ConcurrentHashMap<>();

  private final AtomicInteger threadNumber;
  private final String name;
  private final Set<Thread> threads = Collections.newSetFromMap(new WeakHashMap<>());

  public TestNGThreadFactory(String name) {
    this.name = ThreadUtil.THREAD_NAME + "-" + name + "-";
    this.threadNumber = THREAD_NUMBERS.computeIfAbsent(this.name, k -> new AtomicInteger(1));
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
