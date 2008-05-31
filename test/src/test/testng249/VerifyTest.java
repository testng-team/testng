package test.testng249;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerifyTest extends SimpleBaseTest {

  @Test
  public void verify() {
    XmlSuite suite = new XmlSuite();
    suite.setName("Suite");
    
    XmlTest test = new XmlTest(suite);
    test.setName("Test");
    XmlClass c1 = new XmlClass(B.class);
    c1.setIncludedMethods(Arrays.asList(new String[] { "b"}));
    XmlClass c2 = new XmlClass(Base.class);
    c2.setIncludedMethods(Arrays.asList(new String[] { "b"}));
    test.setXmlClasses(Arrays.asList(new XmlClass[] { c1, c2 }));
    
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { suite }));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }
}
