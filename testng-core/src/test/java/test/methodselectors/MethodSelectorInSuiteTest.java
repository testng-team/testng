package test.methodselectors;

import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import testhelper.OutputDirectoryPatch;

public class MethodSelectorInSuiteTest extends SimpleBaseTest {

  private TestListenerAdapter m_tla;

  @BeforeMethod
  public void setup() {
    m_tla = new TestListenerAdapter();
  }

  @Test
  public void programmaticXmlSuite() {
    TestNG tng = create();
    XmlSuite suite = new XmlSuite();
    XmlMethodSelector methodSelector = new XmlMethodSelector();
    methodSelector.setName("test.methodselectors.Test2MethodSelector");
    methodSelector.setPriority(-1);
    List<XmlMethodSelector> methodSelectors = Lists.newArrayList();
    methodSelectors.add(methodSelector);
    suite.setMethodSelectors(methodSelectors);
    XmlTest test = new XmlTest(suite);
    XmlClass testClass = new XmlClass(test.methodselectors.SampleTest.class);
    test.setXmlClasses(Collections.singletonList(testClass));
    tng.setXmlSuites(Collections.singletonList(suite));
    tng.addListener((ITestNGListener) m_tla);
    tng.run();

    validate(new String[] {"test2"});
  }

  @Test
  public void xmlXmlSuite() {
    TestNG tng = create();
    tng.setTestSuites(Collections.singletonList(getPathToResource("methodselector-in-xml.xml")));
    tng.addListener((ITestNGListener) m_tla);
    tng.run();

    validate(new String[] {"test2"});
  }

  @Test
  public void fileOnCommandLine() {
    String[] args =
        new String[] {
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          getPathToResource("methodselector-in-xml.xml")
        };
    TestNG.privateMain(args, m_tla);

    validate(new String[] {"test2"});
  }

  private void validate(String[] expectPassed) {
    List<ITestResult> passed = m_tla.getPassedTests();
    Assert.assertEquals(passed.size(), expectPassed.length);
    // doing this index based is probably not the best
    for (int i = 0; i < expectPassed.length; i++) {
      Assert.assertEquals(passed.get(i).getName(), expectPassed[i]);
    }
  }
}
