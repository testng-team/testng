package test.testng249;

import java.util.Arrays;
import java.util.Collections;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class VerifyTest extends SimpleBaseTest {

  @Test
  public void verify() {
    XmlSuite suite = new XmlSuite();
    suite.setName("Suite");

    XmlTest test = new XmlTest(suite);
    test.setName("Test");
    XmlClass c1 = new XmlClass(B.class);
    c1.setIncludedMethods(Collections.singletonList(new XmlInclude("b")));
    XmlClass c2 = new XmlClass(Base.class);
    c2.setIncludedMethods(Collections.singletonList(new XmlInclude("b")));
    test.setXmlClasses(Arrays.asList(c1, c2));

    TestNG tng = new TestNG();
    tng.setXmlSuites(Collections.singletonList(suite));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }
}
