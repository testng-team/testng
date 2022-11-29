package test.dependent.issue550;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigDependencySample {

  @BeforeMethod(dependsOnMethods = "anotherBeforeMethod")
  public void beforeMethod() {}

  @BeforeMethod
  public void anotherBeforeMethod() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(100);
  }

  @Test
  public void testMethod() {}
}
