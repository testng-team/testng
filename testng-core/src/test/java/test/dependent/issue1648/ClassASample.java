package test.dependent.issue1648;

import java.util.List;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class ClassASample implements LogExtractor {
  private final List<String> logs = Lists.newArrayList();

  @Test
  protected void test1() {
    addLog("A TestOne 1");
  }

  @Test(dependsOnMethods = {"test1"})
  protected void test2() {
    addLog("A test Two");
  }

  @Override
  public List<String> getLogs() {
    return logs;
  }

  protected void addLog(String log) {
    logs.add(log);
  }
}
