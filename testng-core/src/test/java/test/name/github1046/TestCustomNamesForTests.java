package test.name.github1046;

import java.util.Arrays;
import java.util.Set;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Sets;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class TestCustomNamesForTests extends SimpleBaseTest {
  @Test
  public void testCustomTestNames() {
    XmlSuite xmlSuite = createXmlSuite("Suite", "Test", TestClassSample.class);
    TestNG tng = create(xmlSuite);
    LocalTestNameGatherer reporter = new LocalTestNameGatherer();
    tng.addListener(reporter);
    tng.run();
    Set<String> expectedNames = Sets.newHashSet();
    for (String method : Arrays.asList("testSample1", "testSample2")) {
      for (int i = 1; i <= 5; i++) {
        expectedNames.add(method + "_TestNG_TestCase_" + i);
      }
    }
    expectedNames.add("ordinaryTestMethod_TestNG_TestCase_999");
    expectedNames.add("dontChangeName");
    Assert.assertEqualsNoOrder(reporter.getTestnames(), expectedNames);
  }
}
