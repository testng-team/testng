package test.configurationfailurepolicy.issue2862;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AnnotationBeforeTestSample {

  public static class AnnotatedClassSample {

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
}
