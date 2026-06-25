package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import test.SimpleBaseTest;

public class GroupByInstancesTest extends SimpleBaseTest {

  @Test
  public void dontGroupByInstances() {
    runTest(false);
  }

  @Test
  public void groupByInstances() {
    runTest(true);
  }

  private void runTest(boolean group) {
    TestNG tng = create(GroupByInstancesSampleTest.class);
    GroupByInstancesSampleTest.m_log = Lists.newArrayList();
    tng.setGroupByInstances(group);
    tng.run();

    List<String> log = GroupByInstancesSampleTest.m_log;
    int i = 0;
    if (group) {
      assertThat(log.get(i++)).startsWith("signIn");
      assertThat(log.get(i++)).startsWith("signOut");
      assertThat(log.get(i++)).startsWith("signIn");
      assertThat(log.get(i++)).startsWith("signOut");
    } else {
      assertThat(log.get(i++)).startsWith("signIn");
      assertThat(log.get(i++)).startsWith("signIn");
      assertThat(log.get(i++)).startsWith("signOut");
      assertThat(log.get(i++)).startsWith("signOut");
    }
  }
}
