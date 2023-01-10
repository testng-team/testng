package test.configurationfailurepolicy.issue2862;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AnnotationBeforeTestMultipleTestsSample {

  public static class FirstTestTagAnnotatedClassSample {

    @BeforeTest(ignoreFailure = true)
    public void beforeTest() {
      Assert.fail();
    }

    @BeforeMethod
    public void beforeMethod() {}

    @Test
    public void testA() {}

    @Test
    public void testB() {}
  }

  public static class SeconTestTagAnnotatedClassSample {

    @BeforeMethod
    public void beforeMethod() {}

    @Test
    public void testA() {}

    @Test
    public void testB() {}
  }
}
