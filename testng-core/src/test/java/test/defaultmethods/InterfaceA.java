package test.defaultmethods;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public interface InterfaceA {

  @Test
  default void defaultMethodTest() {}

  @BeforeClass
  default void beforeClassRun() {}

  @AfterClass
  default void afterClassRun() {}

  @BeforeMethod
  default void beforeMethodRun() {}

  @AfterMethod
  default void afterMethodRun() {}
}
