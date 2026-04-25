package test.factory.issue326;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    Map<String, List<Statistics>> results = listener.getResults();

    assertThat(results).containsOnlyKeys(SampleTestClass.FREDDY, SampleTestClass.BARNEY);
    assertThat(results.get(SampleTestClass.FREDDY)).hasSize(2);
    assertThat(results.get(SampleTestClass.BARNEY)).hasSize(2);

    assertThat(threadIdsFor(results, SampleTestClass.FREDDY)).hasSize(1);
    assertThat(threadIdsFor(results, SampleTestClass.BARNEY)).hasSize(1);

    long threadIdFreddyInstance = listener.getThreadIds().get(SampleTestClass.FREDDY);
    long threadIdBarneyInstance = listener.getThreadIds().get(SampleTestClass.BARNEY);
    assertThat(threadIdFreddyInstance).isNotEqualTo(threadIdBarneyInstance);

    assertThat(executionWindowsOverlap(results, SampleTestClass.FREDDY, SampleTestClass.BARNEY))
        .isTrue();
  }

  private static Collection<Long> threadIdsFor(
      Map<String, List<Statistics>> allStats, String instanceName) {
    return allStats.get(instanceName).stream()
        .map(statistics -> statistics.threadId)
        .distinct()
        .collect(Collectors.toList());
  }

  private static boolean executionWindowsOverlap(
      Map<String, List<Statistics>> allStats, String firstInstanceName, String secondInstanceName) {
    long firstStartTime = getStartTimeFrom(allStats, firstInstanceName);
    long firstEndTime = getEndTimeFrom(allStats, firstInstanceName);
    long secondStartTime = getStartTimeFrom(allStats, secondInstanceName);
    long secondEndTime = getEndTimeFrom(allStats, secondInstanceName);
    return firstStartTime < secondEndTime && secondStartTime < firstEndTime;
  }

  private static long getStartTimeFrom(
      Map<String, List<Statistics>> allStats, String instanceName) {
    return allStats.get(instanceName).stream()
        .mapToLong(statistics -> statistics.startTimeInMs)
        .min()
        .orElseThrow(IllegalStateException::new);
  }

  private static long getEndTimeFrom(Map<String, List<Statistics>> allStats, String instanceName) {
    return allStats.get(instanceName).stream()
        .mapToLong(statistics -> statistics.endTimeInMs)
        .max()
        .orElseThrow(IllegalStateException::new);
  }
}
