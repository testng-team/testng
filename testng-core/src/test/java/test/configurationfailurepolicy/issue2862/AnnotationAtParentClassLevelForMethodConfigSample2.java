package test.configurationfailurepolicy.issue2862;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AnnotationAtParentClassLevelForMethodConfigSample2 {

  public static class MyBaseClassSample {
    @BeforeTest(ignoreFailure = true)
    public void beforeTest() {
      fail();
    }

    @BeforeClass
    public void beforeClassInBaseClass() {
      fail();
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
