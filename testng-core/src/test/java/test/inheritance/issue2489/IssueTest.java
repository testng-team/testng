package test.inheritance.issue2489;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.inheritance.issue2489.tests.BaseClassA;
import test.inheritance.issue2489.tests.TestClassA;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2489")
  public void verifyConfigInvocationOrderInInheritance() {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("2489_suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "2489_test");
    XmlPackage xmlPackage = new XmlPackage();
    xmlPackage.setName(TestClassA.class.getPackage().getName() + ".*");
    xmlTest.setPackages(Collections.singletonList(xmlPackage));
    TestNG testng = create(xmlSuite);
    testng.run();
    List<String> expected =
        Arrays.asList(
            "beforeSuite_BaseClass_A",
            "beforeClasses_BaseClass_A",
            "beforeClass_TestClass_A",
            "beforeMethod_TestClass_A",
            "test1_TestClass_A",
            "afterMethod_TestClass_A",
            "beforeMethod_TestClass_A",
            "test2_TestClass_A",
            "afterMethod_TestClass_A",
            "afterClass_TestClass_A",
            "afterClasses_BaseClass_A",
            "afterSuite_BaseClass_A");
    assertThat(BaseClassA.logs).containsExactlyElementsOf(expected);
  }
}
