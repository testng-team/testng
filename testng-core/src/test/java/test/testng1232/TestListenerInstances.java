package test.testng1232;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.testng1232.TestListenerFor1232.CounterTypes;

public class TestListenerInstances extends SimpleBaseTest {

  @Test
  public void testIfOnlyOneListenerInstanceExists() {
    runTestForTestClass(TestClassContainer.SimpleTestClass.class);
  }

  @Test
  public void testIfOnlyOneListenerInstanceExistsUsingAnnotations() {
    runTestForTestClass(TestClassContainer.SimpleTestClassWithListener.class);
  }

  @Test
  public void testIfOnlyOneListenerInstanceExistsUsingListenerTag() {
    runTestForTestClass(TestClassContainer.SimpleTestClass.class, true);
  }

  private static void runTestForTestClass(Class<?> clazz) {
    runTestForTestClass(clazz, false);
  }

  private static void runTestForTestClass(Class<?> clazz, boolean injectListenerViaTag) {
    TestNG tng = createTestNGInstanceFor(clazz, injectListenerViaTag);
    TestListenerFor1232.resetCounters();
    TestListenerFor1232 listener = new TestListenerFor1232();
    tng.addListener((ITestNGListener) listener);
    TestListenerFor1232 anotherListener = new TestListenerFor1232();
    tng.addListener((ITestNGListener) anotherListener);
    tng.run();
    for (CounterTypes type : CounterTypes.values()) {
      Assert.assertEquals(TestListenerFor1232.counters.get(type).intValue(), 1);
    }
  }

  private static TestNG createTestNGInstanceFor(Class<?> clazz, boolean addListenerTag) {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    if (addListenerTag) {
      xmlSuite.addListener(TestListenerFor1232.class.getName());
    }
    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
    createXmlClass(xmlTest, clazz);
    return create(xmlSuite);
  }
}
