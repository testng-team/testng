package test.parameters;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

public class ShadowTest extends SimpleBaseTest {

  @Test
  public void parametersShouldNotBeShadowed() {
    XmlSuite s = createXmlSuite("s");
    XmlTest t = createXmlTest(s, "t");

    {
      XmlClass c1 = new XmlClass(Shadow1SampleTest.class.getName());
      XmlInclude include1 = new XmlInclude("test1");
      include1.setXmlClass(c1);
      c1.getLocalParameters().put("a", "First");
      c1.getIncludedMethods().add(include1);
      t.getXmlClasses().add(c1);
    }

    {
      XmlClass c2 = new XmlClass(Shadow2SampleTest.class.getName());
      XmlInclude include2 = new XmlInclude("test2");
      include2.setXmlClass(c2);
      c2.getLocalParameters().put("a", "Second");
      c2.getIncludedMethods().add(include2);
      t.getXmlClasses().add(c2);
    }

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(s));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

//    System.out.println(s.toXml());
    assertTestResultsEqual(tla.getPassedTests(), Arrays.asList("test1", "test2"));
  }
}
