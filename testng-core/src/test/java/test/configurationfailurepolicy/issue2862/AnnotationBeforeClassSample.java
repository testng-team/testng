package test.configurationfailurepolicy.issue2862;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationBeforeClassSample {

  public static class AnnotatedClassSample {

    @BeforeClass(ignoreFailure = true)
    public void beforeClass() {
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
