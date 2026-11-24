package org.testng.timeout.issue2091;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.timeout.samples.issue2091.OverlappingRowsSample;
import test.SimpleBaseTest;

/**
 * TestNG runs each invocation of a timed method on a new executor. A row that times out and ignores
 * the interruption keeps its thread, so the next row runs beside it. Each executor used to start
 * its thread numbers at 1, which gave both threads the same name.
 */
public class Issue2091Test extends SimpleBaseTest {

  @Test(description = "GITHUB-2091")
  public void overlappingTimedInvocationsRunOnThreadsWithDistinctNames() {
    OverlappingRowsSample.reset();
    TestNG testng = create(OverlappingRowsSample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();

    assertThat(tla.getFailedTests())
        .singleElement()
        .extracting(ITestResult::getThrowable)
        .isInstanceOf(ThreadTimeoutException.class);
    assertThat(tla.getPassedTests()).hasSize(1);
    assertThat(OverlappingRowsSample.firstRowAliveWhenSecondStarted()).isTrue();
    Thread first = OverlappingRowsSample.firstRowThread();
    Thread second = OverlappingRowsSample.secondRowThread();
    assertThat(first).isNotNull().isNotSameAs(second);
    assertThat(second).isNotNull();
    assertThat(second.getName()).isNotEqualTo(first.getName());
  }
}
