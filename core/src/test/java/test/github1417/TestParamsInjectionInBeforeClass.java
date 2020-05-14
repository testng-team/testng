package test.github1417;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestParamsInjectionInBeforeClass {
  @Test
  public void testMethod() {
    String suite = "src/test/resources/parametertest/1417.xml";
    TestNG testNG = new TestNG();
    testNG.setTestSuites(Collections.singletonList(suite));
    testNG.run();
    Assert.assertFalse(testNG.hasFailure());
    Assert.assertFalse(testNG.hasSkip());
    Assert.assertEquals(AnotherTestClassSample.getInstance().getBrowsername(), "chrome");
    List<String> actual = YetAnotherTestClassSample.getInstance().getBrowsers();
    Assert.assertEquals(actual.size(), 2);
    Assert.assertEquals(actual, Arrays.asList("safari", "safari"));
  }
}
