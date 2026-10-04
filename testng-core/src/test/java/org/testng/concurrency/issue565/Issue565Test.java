package org.testng.concurrency.issue565;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.concurrency.samples.issue565.deadlock.ClassInGroupB;
import org.testng.concurrency.samples.issue565.deadlock.GeneratedClassFactory;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;

public class Issue565Test extends SimpleBaseTest {

  /** How long the run may take before the test calls it a deadlock. */
  private static final long LIMIT_SECONDS = 120;

  @Test(description = "GITHUB-565")
  public void aGroupDependencyWithGroupByInstancesDoesNotDeadlock() throws Exception {
    XmlSuite suite = createXmlSuite("Deadlock-Suite");
    suite.setParallel(ParallelMode.CLASSES);
    suite.setThreadCount(5);
    suite.setGroupByInstances(true);
    createXmlTestWithPackages(suite, "Deadlock-Test", ClassInGroupB.class);

    TestNG tng = create(suite);
    TestListenerAdapter listener = new TestListenerAdapter();
    tng.addListener((ITestNGListener) listener);

    // A wall clock guards the run, not a TestNG timeout on the suite.
    //
    // The earlier version gave every method in the inner suite 1000 milliseconds and called that
    // "prevent real deadlock". It also made a slow run look like one, which is why the class sat
    // commented out of testng.xml with a note about a random failure. The scenario really does
    // stall. Over 1000 runs on an idle machine it took 0.05 seconds most times, 6.6 seconds once
    // in about 300, and 18.4 seconds once. A stall inside a method with a 1000 millisecond
    // timeout fails that method, and the counts below then disagree for a reason that is not a
    // deadlock. So the inner suite carries no timeout and its methods are free to be slow.
    //
    // The runner thread is a daemon, and that is what lets the JVM exit after a real deadlock.
    // shutdownNow() only interrupts, and a thread blocked on a lock ignores that, so a non-daemon
    // worker would hold the JVM open after this test had already reported its failure.
    // TestNGThreadFactory does not set daemon, and a new thread inherits its creator's status, so
    // every thread the inner run starts below this one is a daemon too.
    ExecutorService runner =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "issue565-run");
              t.setDaemon(true);
              return t;
            });
    try {
      Future<?> finished = runner.submit((Runnable) tng::run);
      try {
        finished.get(LIMIT_SECONDS, TimeUnit.SECONDS);
      } catch (TimeoutException timedOut) {
        fail(
            "The run did not finish in %d seconds, so the group dependency deadlocked.%n%s",
            LIMIT_SECONDS, lockedThreads());
      } catch (ExecutionException crashed) {
        // The run threw instead of finishing. That is neither a deadlock nor a failed method, and
        // without this the test reports an ExecutionException that a reader has to unwrap.
        fail(
            "The run threw instead of finishing, so no count below means anything",
            crashed.getCause());
      }
    } finally {
      runner.shutdownNow();
    }

    assertThat(listener.getFailedTests())
        .withFailMessage("The run finished, so no method may have failed")
        .isEmpty();
    assertThat(listener.getSkippedTests())
        .withFailMessage("A skipped method means the group dependency did not release")
        .isEmpty();
    assertThat(listener.getPassedTests()).hasSize(2 + 4 * GeneratedClassFactory.SIZE);
  }

  /**
   * Names the threads that are blocked on a lock another one holds, so a failure says where the run
   * stopped. Returns a sentence saying so when the JVM finds none.
   */
  private static String lockedThreads() {
    ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    long[] ids = threads.findDeadlockedThreads();
    if (ids == null) {
      return "No thread is blocked on a lock another holds, so the run is stuck elsewhere.";
    }
    // getThreadInfo puts null in the slot of a thread that ended between the two calls. Mapping
    // toString over that null would replace this whole report with a NullPointerException, and
    // the deadlock the test had just caught would be lost.
    String report =
        Arrays.stream(threads.getThreadInfo(ids, true, true))
            .filter(Objects::nonNull)
            .map(ThreadInfo::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    return report.isEmpty()
        ? "The JVM named blocked threads, and every one of them ended before it could be read."
        : report;
  }
}
