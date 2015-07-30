package test.methodinterceptors;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.xml.sax.SAXException;

import test.SimpleBaseTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class MethodInterceptorTest extends SimpleBaseTest {

  private String XML =
    "<!DOCTYPE suite SYSTEM \"http://beust.com/testng/testng-1.0.dtd\" >" +
    "" +
    "<suite name=\"Single\" verbose=\"0\">" +
    "" +
    "<listeners>" +
    "  <listener class-name=\"test.methodinterceptors.NullMethodInterceptor\" />" +
    "</listeners>" +
    "" +
    "  <test name=\"Single\" >" +
    "    <classes>" +
    "      <class name=\"test.methodinterceptors.FooTest\" />" +
    "     </classes>" +
    "  </test>" +
    "" +
    "</suite>";

  @Test
  public void noMethodsShouldRun() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { FooTest.class });
    testNullInterceptor(tng);
  }

  private void testNullInterceptor(TestNG tng) {
    tng.setMethodInterceptor(new NullMethodInterceptor());
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 0);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
    Assert.assertEquals(tla.getSkippedTests().size(), 0);
  }

  private void testFast(boolean useInterceptor) {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { FooTest.class });
    if (useInterceptor) {
      tng.setMethodInterceptor(new FastTestsFirstInterceptor());
    }
    TestListenerAdapter tla = new TestListenerAdapter();
//    tng.setParallel("methods");
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 3);
    ITestResult first = tla.getPassedTests().get(0);

    String method = "zzzfast";
    if (useInterceptor) {
      Assert.assertEquals(first.getMethod().getMethodName(), method);
    } else {
      Assert.assertNotSame(first.getMethod().getMethodName(), method);
    }
  }

  @Test
  public void fastShouldRunFirst() {
    testFast(true /* use interceptor */);
  }

  @Test
  public void fastShouldNotRunFirst() {
    testFast(false /* don't use interceptor */);
  }

  @Test
  public void nullMethodInterceptorWorksInTestngXml()
      throws IOException, ParserConfigurationException, SAXException {

    File f = File.createTempFile("testng-tests-", "");
    f.deleteOnExit();
    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
    bw.write(XML);
    bw.close();

    try {
      List<XmlSuite> xmlSuites = new Parser(f.getAbsolutePath()).parseToList();

      TestNG tng = create();
      tng.setXmlSuites(xmlSuites);
      testNullInterceptor(tng);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  @Test(timeOut = 1000)
  public void shouldNotLockUpWithInterceptorThatRemovesMethods() {
    TestNG tng = create(LockUpInterceptorSampleTest.class);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    tng.run();
  }
}
