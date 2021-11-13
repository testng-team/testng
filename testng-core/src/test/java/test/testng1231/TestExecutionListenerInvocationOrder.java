package test.testng1231;

import com.beust.jcommander.internal.Lists;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.testng.*;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class TestExecutionListenerInvocationOrder extends SimpleBaseTest {
  @Test
  public void testListenerOrder() {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
    createXmlClass(xmlTest, ListenerOrderTestSample.class);
    TestNG tng = create(xmlSuite);
    TestListenerFor1231 listener = new TestListenerFor1231();
    tng.addListener((ITestNGListener) listener);
    tng.run();
    List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);
    Assert.assertEquals(TestListenerFor1231.order, expected);
  }

  public static class TestListenerFor1231
      implements IExecutionListener, IAlterSuiteListener, IReporter, ISuiteListener {
    public static LinkedList<Integer> order = Lists.newLinkedList();

    @Override
    public void onExecutionStart() {
      order.add(1);
    }

    @Override
    public void onExecutionFinish() {
      order.add(6);
    }

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      order.add(5);
    }

    @Override
    public void alter(List<XmlSuite> suites) {
      order.add(2);
    }

    @Override
    public void onStart(ISuite suite) {
      order.add(3);
    }

    @Override
    public void onFinish(ISuite suite) {
      order.add(4);
    }
  }
}
