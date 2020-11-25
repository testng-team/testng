package test.guice.config;

import test.guice.config.modules.TestModuleOne;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {TestModuleOne.class})
@Test()
public class Test1 {
  public void shouldInstatiateModulesOnlyOnce() {
    System.out.println(String.format("Test %s called", getClass().getSimpleName()));
  }
}