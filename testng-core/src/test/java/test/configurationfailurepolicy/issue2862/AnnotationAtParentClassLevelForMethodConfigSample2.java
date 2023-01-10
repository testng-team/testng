package test.configurationfailurepolicy.issue2862;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AnnotationAtParentClassLevelForMethodConfigSample2 {

  public static class MyBaseClassSample {
    @BeforeTest(ignoreFailure = true)
    public void beforeTest() {
      Assert.fail();
    }

    @BeforeClass
    public void beforeClassInBaseClass() {
      Assert.fail();
    }
  }

  public static class AnnotatedClassSample extends MyBaseClassSample {

    @BeforeMethod
    public void beforeMethodInChildTestClass() {}

    @Test
    public void testA() {}

    @Test
    public void testB() {}
  }
}
