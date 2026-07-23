package test.configurationfailurepolicy.issue2862;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationBeforeMethodSample {

  public static class AnnotatedClassSample {
    private boolean fail = true;

    @BeforeMethod(ignoreFailure = true)
    public void beforeMethod1() {
      if (fail) {
        fail = false;
        fail();
      }
    }

    @Test
    public void testA() {}

    @Test
    public void testB() {}
  }
}
