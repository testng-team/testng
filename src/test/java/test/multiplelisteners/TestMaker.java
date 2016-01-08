package test.multiplelisteners;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestMaker
{
  @Test(description = "Make sure only one listener is created and not 2^3")
  public void run()
  {
    final TestNG tng = new TestNG();
    tng.setUseDefaultListeners(false);
    tng.setListenerClasses(Arrays.<Class> asList(TestListenerAdapter.class, SimpleReporter.class));
    final List<XmlSuite> suites = createSuites();
    tng.setXmlSuites(suites);
    tng.setVerbose(0);
    tng.run();

//    Reporter.log(tng.getSuiteListeners().size() + "", true);
//    for (final XmlSuite xmlSuite : suites)
//    {
//      Reporter.log(xmlSuite.getName() + ": " + xmlSuite.getListeners().size(), true);
//    }
  }

  private List<XmlSuite> createSuites()
  {
    final List<XmlSuite> ret = Lists.newArrayList();
    for (int i = 0; i < 3; i++)
    {
      ret.add(createSuite(i));
    }
    return ret;
  }

  private XmlSuite createSuite(final int nr)
  {
    final XmlSuite suite = new XmlSuite();
    suite.setName("Suite_" + nr);

    new XmlTest(suite).setXmlClasses(Collections.singletonList(new XmlClass(Test1.class)));
    return suite;
  }
}
