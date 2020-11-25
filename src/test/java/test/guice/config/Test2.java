package test.guice.config;

import test.guice.config.modules.TestModuleTwo;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {TestModuleTwo.class})
@Test()
public class Test2 {
  public void shouldInstatiateModulesOnlyOnce() {
    System.out.println(String.format("Test %s called", getClass().getSimpleName()));
  }
}
