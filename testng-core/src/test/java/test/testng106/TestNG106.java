package test.testng106;

import java.util.List;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class TestNG106 extends SimpleBaseTest {
  @Test
  public void testFailingBeforeSuiteShouldSkipAllTests() {
    TestNG tng = create();
    XmlSuite s = createXmlSuite("TESTNG-106");
    createXmlTest(s, "myTest1", FailingSuiteFixture.class.getName(), Test1.class.getName());
    createXmlTest(s, "myTest2", Test1.class.getName());
    createXmlTest(s, "myTest3", Test2.class.getName());
    createXmlTest(s, "myTest-last", Test2.class.getName());
    tng.setXmlSuites(List.of(s));
    tng.run();
    Assert.assertEquals(
        FailingSuiteFixture.s_invocations,
        0,
        "@BeforeSuite has failed. All tests should be skipped.");
  }
}
