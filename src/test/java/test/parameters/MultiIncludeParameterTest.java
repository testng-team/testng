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

public class MultiIncludeParameterTest extends SimpleBaseTest {

  @Test
  public void multiIncludeParameterTest() {
    XmlSuite s = createXmlSuite("s");
    XmlTest t = createXmlTest(s, "t");

    {
      XmlClass c1 = new XmlClass(MultiIncludeSampleTest.class.getName());
      int index = 0;
      XmlInclude include1 = new XmlInclude("multiIncludeTest", index++);
      include1.addParameter("num", "1");
      XmlInclude include2 = new XmlInclude("multiIncludeTest", index++);
      include2.addParameter("num", "2");
      c1.getIncludedMethods().add(include1);
      c1.getIncludedMethods().add(include2);
      t.getXmlClasses().add(c1);
    }

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(s));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertTestResultsEqual(tla.getPassedTests(), Arrays.asList("multiIncludeTest", "multiIncludeTest"));
  }
}
