package test.guice.issue2427;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import test.guice.issue2427.modules.TestModuleTwo;

@Guice(modules = {TestModuleTwo.class})
@Test()
public class Test2 {
  public void shouldInstatiateModulesOnlyOnce() {
    // do nothing as test is about configuration part
  }
}
