package test.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;
import test.SampleInheritance;
import test.SimpleBaseTest;

public class InheritanceConfigTest extends SimpleBaseTest {

  @Test
  public void testMethod() {
    XmlTest xmlTest = createXmlTest("xml_suite", "xml_test");
    xmlTest.setXmlClasses(Collections.singletonList(new XmlClass(SampleInheritance.class)));
    xmlTest.addIncludedGroup("configuration0");
    xmlTest.addIncludedGroup("configuration1");
    xmlTest.addIncludedGroup("inheritedTestMethod");
    xmlTest.addIncludedGroup("final");
    TestNG tng = create(xmlTest.getSuite());
    tng.run();
    assertThat(tng.getStatus()).isEqualTo(0);
  }
}
