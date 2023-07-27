package org.testng.xml.issue2937;

import org.junit.jupiter.api.Test;
import org.testng.TestNG;
//import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTest {

  @Test()
  public void ensureSuiteLevelBeanshellIsAppliedToAllTests() throws IOException {
    PrintStream current = System.out;
    try {
      Parser parser = new Parser("testng-core/src/test/java/org/testng/xml/issue2937/suite.xml");
      List<XmlSuite> suites = parser.parseToList();
      XmlSuite xmlsuite = suites.get(0);
      assertThat(xmlsuite.getTests().get(0).getMethodSelectors().size()).isEqualTo(0);
      TestNG testNG = new TestNG();
      testNG.setXmlSuites(suites);
      testNG.addListener(new ListenerSetupReporter());
      testNG.run();
      assertThat(xmlsuite.getTests().get(0).getMethodSelectors().size()).isEqualTo(1);
    } finally {
      System.setOut(current);
    }
  }
}
