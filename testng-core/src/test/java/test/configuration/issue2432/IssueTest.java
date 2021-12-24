package test.configuration.issue2432;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2432")
  public void ensureNoImplicitDependencyIsAddedWhenGroupsAreInvolved() {
    XmlClass xmlClass = new XmlClass(Test1.class);
    XmlInclude xmlInclude = new XmlInclude("test1");
    xmlClass.setIncludedMethods(Collections.singletonList(xmlInclude));
    XmlSuite xmlsuite = createXmlSuite("2432_suite");
    xmlsuite.setParallel(ParallelMode.NONE);
    XmlTest xmlTest = new XmlTest(xmlsuite);
    xmlTest.setName("2432_test");
    xmlTest.setXmlClasses(Collections.singletonList(xmlClass));
    TestNG testng = create(xmlsuite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    List<String> expected =
        Arrays.asList(
            "prepareConfig",
            "prepareConfigForTest1",
            "uploadConfigToDatabase",
            "verifyConfigurationAfterInstall",
            "test1");
    assertThat(listener.getInvokedMethodNames()).containsExactlyElementsOf(expected);
  }
}
