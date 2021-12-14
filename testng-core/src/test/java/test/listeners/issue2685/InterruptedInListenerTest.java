package test.listeners.issue2685;

import org.assertj.core.api.Assertions;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class InterruptedInListenerTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2685")
  public void testThreadIsNotInterruptedInListener() {
    XmlSuite xmlSuite = createXmlSuite("2685_suite");
    xmlSuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    createXmlTest(xmlSuite, "2685_test", SampleInterruptedTest.class);
    TestNG testng = create(xmlSuite);
    SampleTestFailureListener listener = new SampleTestFailureListener();
    testng.addListener(listener);
    testng.run();

    Assertions.assertThat(listener.getInterruptedMethods()).isEmpty();
  }
}
