package test.configurationfailurepolicy.issue2862;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationAtParentClassLevelForMethodConfigSample {

  public static class MyBaseClassSample {

    private boolean fail = true;

    @BeforeMethod(ignoreFailure = true)
    public void beforeMethod() {
      if (fail) {
        fail = false;
        Assert.fail();
      }
    }
  }

  public static class AnnotatedClassSample extends MyBaseClassSample {

    @Test
    public void testA() {}

    @Test
    public void testB() {}
  }
}
