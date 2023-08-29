package test.listeners.issue2961;

import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;

public class ParentTestClassSample {

  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethodFirstTimeOnlyParent(Method method) {}
}
