package test.thread.issue2361;

import org.testng.annotations.Factory;

public class FactorySample {

  @Factory
  public static Object[] newInstances() {
    return new Object[] {new ChildClassExample(), new AnotherChildClassExample()};
  }
}
