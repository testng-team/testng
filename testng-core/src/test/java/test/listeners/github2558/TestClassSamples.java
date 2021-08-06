package test.listeners.github2558;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassSamples {

  public static class TestClassSampleA {

    @BeforeClass
    public void beforeClass() {}

    @Test
    public void testMethod() {}

    @AfterClass
    public void afterClass() {}
  }

  public static class TestClassSampleB {

    @BeforeClass
    public void beforeClass() {}

    @Test
    public void testMethod() {}

    @AfterClass
    public void afterClass() {}
  }
}
