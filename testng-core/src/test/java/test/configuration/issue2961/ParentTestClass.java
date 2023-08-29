package test.configuration.issue2961;

import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;

public class ParentTestClass {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethodFirstTimeOnlyParent(Method method) {
    System.out.println("First Time Only Parent " + method);
  }
}
