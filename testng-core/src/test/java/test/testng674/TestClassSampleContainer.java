package test.testng674;

import org.testng.SkipException;
import org.testng.annotations.*;

public class TestClassSampleContainer {
  public static final String ERROR_MSG = "GITHUB-674 Exception";

  public static class SampleClassWithFailingBeforeClassMethod {
    @BeforeClass
    public void beforeClass() {
      throw new RuntimeException(ERROR_MSG);
    }

    @Test
    public void testMethod() {}
  }

  public static class ChildClass extends SampleClassWithFailingBeforeClassMethod {}

  public static class SampleClassWithMultipleFailures {
    @BeforeClass
    public void beforeClass() {
      throw new RuntimeException(ERROR_MSG);
    }

    @BeforeMethod
    public void beforeMethod() {
      throw new RuntimeException(ERROR_MSG);
    }

    @Test
    public void testMethod() {}
  }

  public static class SampleClassWithMultipleFailuresAndAlwaysRun {
    @BeforeClass
    public void beforeClass() {
      throw new RuntimeException(ERROR_MSG);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
      throw new RuntimeException(ERROR_MSG);
    }

    @Test
    public void testMethod() {}
  }

  public static class SampleClassWithExplicitConfigSkip {
    @BeforeClass
    public void beforeClass() {
      throw new SkipException(ERROR_MSG);
    }

    @Test
    public void testMethod() {}
  }

  public static class SuiteFailureTestClass {
    @BeforeSuite
    public void beforeSuite() {
      throw new SkipException(ERROR_MSG);
    }

    @Test
    public void testMethod() {}
  }

  public static class RegularTestClass {
    @Test
    public void testMethod() {}
  }

  public static class GroupsContainer {

    public static class GroupA {
      @BeforeGroups(groups = "foo")
      public void beforeGroups() {
        throw new RuntimeException(ERROR_MSG);
      }

      @Test(groups = "foo")
      public void testMethod() {}
    }

    public static class GroupB {
      @Test(groups = "foo")
      public void testMethod() {}
    }
  }

  public static class BaseSample {

    @BeforeSuite
    public void beforeSuite() {
      throw new RuntimeException(ERROR_MSG);
    }
  }

  public static class A extends BaseSample {

    @Test
    public void testMethod() {}
  }

  public static class B extends BaseSample {

    @Test
    public void testMethod() {}
  }
}
