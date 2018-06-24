package test.factory.issue1745;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class Github1745Test extends SimpleBaseTest {

  private static final String TEST_METHOD = "testMethod";

  @Test
  public void testMethod() {
    XmlTest xmlTest = createXmlTest("1745_suite", "1745_test", SuiteXmlPoweredFactoryTest.class);
    xmlTest.addParameter("number", "3");
    TestNG testng = create();
    testng.setXmlSuites(Collections.singletonList(xmlTest.getSuite()));
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getSucceedMethodNames())
        .containsExactly(TEST_METHOD, TEST_METHOD, TEST_METHOD, TEST_METHOD);
  }
}
