package test.guice.issue2427;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import test.guice.issue2427.modules.TestModuleOne;

@Guice(modules = {TestModuleOne.class})
@Test()
public class Test1 {
  public void shouldInstatiateModulesOnlyOnce() {
    // do nothing as test is about configuration part
  }
}
