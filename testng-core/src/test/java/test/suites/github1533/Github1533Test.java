package test.suites.github1533;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;
import test.SimpleBaseTest;

public class Github1533Test extends SimpleBaseTest {
  @Test
  public void testScenarioWithChildSuites() {
    String suiteFile = "src/test/resources/xml/github1533/parent.xml";
    runTests(suiteFile, 1, 2, "GitHub1533_Suite", "GitHub1533_Parent_Suite");
  }

  @Test
  public void testScenarioWithNoChildSuites() {
    String suiteFile = "src/test/resources/xml/github1533/child.xml";
    runTests(suiteFile, 0, 1, "GitHub1533_Suite");
  }

  private static void runTests(
      String suiteFile, int childSuitesCount, int suiteCounter, String... suiteNames) {
    List<XmlSuite> suites;
    try {
      suites = new Parser(suiteFile).parseToList();
    } catch (IOException e) {
      throw new TestNGException(e);
    }
    assertEquals(suites.size(), 1);
    assertEquals(suites.get(0).getChildSuites().size(), childSuitesCount);
    TestNG testng = create(suites);
    SuiteCounter listener = new SuiteCounter();
    testng.addListener((ITestNGListener) listener);
    testng.run();
    assertEquals(listener.getCounter(), suiteCounter);
    assertThat(listener.getSuiteNames()).containsExactly(suiteNames);
  }
}
