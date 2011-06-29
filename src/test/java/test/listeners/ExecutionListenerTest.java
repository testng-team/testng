package test.listeners;

import org.testng.Assert;
import org.testng.IExecutionListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

public class ExecutionListenerTest extends SimpleBaseTest {

  public static class ExecutionListener implements IExecutionListener {
    public static boolean m_start = false;
    public static boolean m_finish = false;

    @Override
    public void onExecutionStart() {
      m_start = true;
    }

    @Override
    public void onExecutionFinish() {
      m_finish = true;
    }
  }

  @Test
  public void executionListenerWithXml() {
    runTest(ExecutionListener1SampleTest.class, true /* add listener */, true /* should run */);
  }

  @Test
  public void executionListenerWithoutListener() {
    runTest(ExecutionListener1SampleTest.class, false /* don't add listener */,
        false /* should not run */);
  }

  @Test
  public void executionListenerAnnotation() {
    runTest(ExecutionListener2SampleTest.class, false /* don't add listener */,
        true /* should run */);
  }

  private void runTest(Class<?> listenerClass, boolean addListener, boolean expected) {
    XmlSuite s = createXmlSuite("ExecutionListener");
    XmlTest t = createXmlTest(s, "Test", listenerClass.getName());

    if (addListener) {
      s.addListener(ExecutionListener.class.getName());
    }
    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(s));
    ExecutionListener.m_start = false;
    ExecutionListener.m_finish = false;
    tng.run();

    Assert.assertEquals(ExecutionListener.m_start, expected);
    Assert.assertEquals(ExecutionListener.m_finish, expected);
  }
}
