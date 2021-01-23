package test.reports.issue1756;

import java.util.UUID;
import org.testng.ITest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(CustomTestNGReporter.class)
public class SampleTestClass implements ITest {

  private static String uuid = UUID.randomUUID().toString();
  private String uri;

  public SampleTestClass() {
    this.uri = uuid;
  }

  public static String getUuid() {
    return uuid;
  }

  @Test
  public void test1() {
    throw new RuntimeException("failed");
  }

  @Test(dependsOnMethods = "test1")
  public void test2() {
  }

  public String getTestName() {
    return uri;
  }
}