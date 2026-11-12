package org.testng.dependent.issue3201;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.dependent.samples.issue3201.ChainedFactorySample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * GITHUB-3201. Each {@code @Factory} instance runs its {@code dependsOnMethods} chain in order when
 * the methods run in parallel.
 */
public class IssueTest extends SimpleBaseTest {

  @DataProvider
  public static Object[][] groupByInstances() {
    return new Object[][] {{true}, {false}};
  }

  @Test(description = "GITHUB-3201", dataProvider = "groupByInstances")
  public void everyFactoryInstanceRunsItsDependentMethodsInOrder(boolean groupByInstances) {
    ChainedFactorySample.EVENTS.clear();
    TestNG testng = create(ChainedFactorySample.class);
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.setThreadCount(4);
    testng.setGroupByInstances(groupByInstances);
    testng.run();
    for (int index = 0; index < 4; index++) {
      String prefix = index + ".";
      List<String> ofInstance =
          ChainedFactorySample.EVENTS.stream()
              .filter(event -> event.startsWith(prefix))
              .collect(Collectors.toList());
      assertThat(ofInstance)
          .containsExactly(prefix + "a", prefix + "b", prefix + "c", prefix + "d");
    }
  }
}
