package test.issue565;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
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
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;
import test.issue565.deadlock.ClassInGroupB;
import test.issue565.deadlock.GeneratedClassFactory;

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

    // The run is guarded by a wall clock here, not by a TestNG timeout on the suite.
    //
    // The earlier version of this test gave every method in the suite 1000 milliseconds and called
    // that "prevent real deadlock". It also made a slow run look like a deadlock, which is why the
    // class sat commented out of testng.xml with a note about a random failure. The scenario
    // stalls:
    // over 1000 runs on an idle machine the test took 0.05 seconds most times, 6.6 seconds once in
    // about 300, and 18.4 seconds once. Against a 1000 millisecond guard, a stall inside a timed
    // method fails the method, and the counts below then disagree for a reason that is not a
    // deadlock.
    //
    // So the suite now carries no timeout and the methods are free to be slow. A real deadlock does
    // not finish at all, and that is what this waits for.
    ExecutorService runner = Executors.newSingleThreadExecutor(r -> new Thread(r, "issue565-run"));
    try {
      Future<?> finished = runner.submit((Runnable) tng::run);
      try {
        finished.get(LIMIT_SECONDS, TimeUnit.SECONDS);
      } catch (TimeoutException timedOut) {
        fail(
            "The run did not finish in %d seconds, so the group dependency deadlocked.%n%s",
            LIMIT_SECONDS, lockedThreads());
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
    long[] ids = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
    if (ids == null) {
      return "No thread is blocked on a lock another holds, so the run is stuck elsewhere.";
    }
    return Arrays.stream(ManagementFactory.getThreadMXBean().getThreadInfo(ids, true, true))
        .map(ThreadInfo::toString)
        .collect(Collectors.joining(System.lineSeparator()));
  }
}
