package test.listeners.issue2961;

import java.lang.reflect.Method;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OnlyOnceConfigurationThatFailsTestSample extends ParentTestClassSample {

  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethodFirstTimeOnlyTestClass(Method method) {
    throw new RuntimeException("Example");
  }

  @Test
  public void test() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterMethodLastTimeOnly() {}
}
