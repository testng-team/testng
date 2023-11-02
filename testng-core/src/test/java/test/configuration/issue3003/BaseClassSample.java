package test.configuration.issue3003;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;

public class BaseClassSample {
  public static final List<String> logs = new ArrayList<>();

  @BeforeClass(alwaysRun = true)
  public void setupMethod1() {
    logs.add("setupMethod1");
  }

  @BeforeClass(
      alwaysRun = true,
      dependsOnMethods = {"setupMethod1"})
  public void setupMethod2() {
    logs.add("setupMethod2");
  }
}
