package test.inject.issue1994;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

/**
 * Injects the native {@link XmlTest} into both a configuration method and a test method, so the two
 * distinct paths that feed {@code ITestResult.setParameters} are both exercised.
 */
public class XmlTestInjectionSample {

  @BeforeTest
  public void beforeTest(XmlTest xmlTest) {}

  @Test
  public void testMethod(XmlTest xmlTest) {}

  @AfterTest
  public void afterTest(XmlTest xmlTest) {}
}
