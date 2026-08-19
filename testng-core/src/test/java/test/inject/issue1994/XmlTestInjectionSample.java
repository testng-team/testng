package test.inject.issue1994;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

/**
 * Injects the native {@link XmlTest} into both a configuration method and a test method, so the two
 * paths that feed {@code ITestResult.setParameters} are both exercised. The reporter of GITHUB-1994
 * hit this with the injection mixed into an {@code @Optional} parameter list, so one method keeps
 * that shape.
 */
public class XmlTestInjectionSample {

  @BeforeTest
  public void beforeTest(XmlTest xmlTest) {}

  @Test
  public void testMethod(XmlTest xmlTest) {}

  @AfterTest
  public void afterTest(XmlTest xmlTest, @Optional("chrome") String browser) {}
}
