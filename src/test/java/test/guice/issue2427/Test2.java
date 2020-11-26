package test.guice.issue2427;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import test.guice.issue2427.modules.TestModuleTwo;

@Guice(modules = {TestModuleTwo.class})
@Test()
public class Test2 {
  public void shouldInstatiateModulesOnlyOnce() {
    System.out.println(String.format("Test %s called", getClass().getSimpleName()));
  }
}
