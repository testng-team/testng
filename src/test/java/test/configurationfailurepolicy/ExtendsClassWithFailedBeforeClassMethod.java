package test.configurationfailurepolicy;

import org.testng.annotations.Test;

public class ExtendsClassWithFailedBeforeClassMethod extends ClassWithFailedBeforeClassMethod {

  @Test
  public void test2() {
        // should be skipped, but BeforeClass method attempted again
  }
}
