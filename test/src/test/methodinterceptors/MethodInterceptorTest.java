package test.methodinterceptors;

import javax.xml.parsers.ParserConfigurationException;

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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MethodInterceptorTest extends SimpleBaseTest {
  
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

  @Test
  public void fastShouldRunFirst() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { FooTest.class });
    tng.setMethodInterceptor(new FastTestsFirstInterceptor());
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 2);
    ITestResult first = tla.getPassedTests().get(0);
    Assert.assertEquals(first.getMethod().getMethodName(), "fast");
  }
  
  @Test
  public void listenersWorkInTestngXml()
      throws IOException, ParserConfigurationException, SAXException {
    String xml = "<!DOCTYPE suite SYSTEM \"http://beust.com/testng/testng-1.0.dtd\" >" +
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

    File f = File.createTempFile("testng-tests-", "");
    f.deleteOnExit();
    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
    bw.write(xml);
    bw.close();
    
    FileInputStream fis = null;
    try {
      List<XmlSuite> xmlSuites = new Parser(f.getAbsolutePath()).parseToList();
      
      TestNG tng = create();
      tng.setXmlSuites(xmlSuites);
      testNullInterceptor(tng);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    finally {
      if (fis != null) fis.close();
    }
  }

}
