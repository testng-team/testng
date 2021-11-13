package test.testng674;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class BeforeClassSkipExceptionTest extends SimpleBaseTest {

  @Test
  public void testIfTestMethodHasException() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    createTestNGInstanceAndRun(
        reporter, TestClassSampleContainer.SampleClassWithFailingBeforeClassMethod.class);
  }

  @Test
  public void testIfTestMethodHasExceptionInInheritance() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    createTestNGInstanceAndRun(reporter, TestClassSampleContainer.ChildClass.class);
  }

  @Test
  public void testExceptionDetailsWhenClassHasMultipleFailures() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    createTestNGInstanceAndRun(
        reporter, TestClassSampleContainer.SampleClassWithMultipleFailures.class);
  }

  @Test
  public void testExceptionDetailsWhenClassHasExplicitSkipInConfiguration() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    createTestNGInstanceAndRun(
        reporter, TestClassSampleContainer.SampleClassWithExplicitConfigSkip.class);
  }

  @Test
  public void testExceptionDetailsWhenConfigHasAlwaysRun() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    createTestNGInstanceAndRun(
        reporter, TestClassSampleContainer.SampleClassWithMultipleFailuresAndAlwaysRun.class);
  }

  @Test
  public void testExceptionDetailsUsingGroupsWithFailures() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    Class<?>[] classes = {
      TestClassSampleContainer.GroupsContainer.GroupA.class,
      TestClassSampleContainer.GroupsContainer.GroupB.class
    };
    createTestNGInstanceAndRun(reporter, 2, true, classes);
  }

  @Test
  public void testExceptionDetailsWhenFailuresExistInSuiteConfigs() {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    XmlTest xmlTest1 = createXmlTest(xmlSuite, "Test1");
    createXmlClass(xmlTest1, TestClassSampleContainer.SuiteFailureTestClass.class);
    XmlTest xmlTest2 = createXmlTest(xmlSuite, "Test2");
    createXmlClass(xmlTest2, TestClassSampleContainer.RegularTestClass.class);
    TestNG tng = create(xmlSuite);
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    tng.addListener((ITestNGListener) reporter);
    tng.run();
    Assert.assertEquals(reporter.getErrors().size(), 2);
    for (Throwable error : reporter.getErrors()) {
      Assert.assertEquals(error.getMessage(), TestClassSampleContainer.ERROR_MSG);
      Assert.assertTrue(error instanceof RuntimeException);
    }
  }

  @Test
  public void testExceptionDetailsWhenFailuresExistInABaseClass() {
    ReportingListenerFor674 reporter = new ReportingListenerFor674();
    Class<?>[] classes = {TestClassSampleContainer.A.class, TestClassSampleContainer.B.class};
    createTestNGInstanceAndRun(reporter, 2, false, classes);
  }

  private static void createTestNGInstanceAndRun(
      ReportingListenerFor674 reporter, Class<?>... clazz) {
    createTestNGInstanceAndRun(reporter, 1, false, clazz);
  }

  private static void createTestNGInstanceAndRun(
      ReportingListenerFor674 reporter, int expectedCount, boolean useGroups, Class<?>... clazzes) {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
    if (useGroups) {
      xmlTest.addIncludedGroup("foo");
    }
    for (Class<?> clazz : clazzes) {
      createXmlClass(xmlTest, clazz);
    }
    TestNG tng = create(xmlSuite);
    tng.addListener((ITestNGListener) reporter);
    tng.run();
    Assert.assertEquals(reporter.getErrors().size(), expectedCount);
    for (Throwable error : reporter.getErrors()) {
      Assert.assertEquals(error.getMessage(), TestClassSampleContainer.ERROR_MSG);
      Assert.assertTrue(error instanceof RuntimeException);
    }
  }
}
