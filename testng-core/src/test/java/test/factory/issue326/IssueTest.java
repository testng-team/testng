package test.factory.issue326;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;
import test.factory.issue326.LocalTrackingListener.Statistics;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-326")
  public void testToCheckParallelExecutionOfInstancesWithOneThreadPerInstance() {
    TestNG testng = create(SampleTestClass.class);
    testng.setGroupByInstances(true);
    testng.setParallel(ParallelMode.INSTANCES);
    testng.setThreadCount(20);
    testng.setDataProviderThreadCount(10);
    LocalTrackingListener listener = new LocalTrackingListener();
    testng.addListener(listener);
    testng.run();

    // Ensure that the difference in start times for the methods in both instances is <= 1000 ms.
    long diff = computeDiffInStartTimeFor(listener.getResults(), SampleTestClass.FREDDY);
    assertThat(diff).isLessThanOrEqualTo(1000);
    diff = computeDiffInStartTimeFor(listener.getResults(), SampleTestClass.BARNEY);
    assertThat(diff).isLessThanOrEqualTo(1000);

    // Ensure that the thread ids for both the instances are different.
    long threadIdFreddyInstance = listener.getThreadIds().get(SampleTestClass.FREDDY);
    long threadIdBarneyInstance = listener.getThreadIds().get(SampleTestClass.BARNEY);

    assertThat(threadIdFreddyInstance).isNotEqualTo(threadIdBarneyInstance);
  }

  private static long computeDiffInStartTimeFor(
      Map<String, List<Statistics>> allStats, String instanceName) {
    long test1OnFreddyInstance = getStartTimeFrom(allStats, instanceName, "test1");
    long test2OnFreddyInstance = getStartTimeFrom(allStats, instanceName, "test2");
    return Math.abs(test2OnFreddyInstance - test1OnFreddyInstance);
  }

  private static long getStartTimeFrom(
      Map<String, List<Statistics>> allStats, String instanceName, String methodName) {
    return allStats.get(instanceName).stream()
        .filter(statistics -> statistics.methodName.equals(methodName))
        .findFirst()
        .map(statistics -> statistics.startTimeInMs)
        .orElseThrow(IllegalStateException::new);
  }
}
