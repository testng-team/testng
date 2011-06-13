package test.dependent;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import test.SimpleBaseTest;

import java.util.List;

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
      Assert.assertTrue(log.get(i++).startsWith("signIn"));
      Assert.assertTrue(log.get(i++).startsWith("signOut"));
      Assert.assertTrue(log.get(i++).startsWith("signIn"));
      Assert.assertTrue(log.get(i++).startsWith("signOut"));
    } else {
      Assert.assertTrue(log.get(i++).startsWith("signIn"));
      Assert.assertTrue(log.get(i++).startsWith("signIn"));
      Assert.assertTrue(log.get(i++).startsWith("signOut"));
      Assert.assertTrue(log.get(i++).startsWith("signOut"));
    }
  }
}
