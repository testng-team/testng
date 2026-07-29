package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.thread.issue3066.Issue3066ExecutorServiceFactory;
import test.thread.issue3066.Issue3066ThreadPoolExecutor;
import test.thread.issue3066.TestClassSample;

public class CustomExecutorServiceFactoryTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3066")
  public void ensureCanWireInCustomExecutorServiceWhenEnabledViaAPI() {
    TestNG testng = create(TestClassSample.class);
    testng.setExecutorServiceFactory(new Issue3066ExecutorServiceFactory());
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.run();
    assertThat(Issue3066ThreadPoolExecutor.isInvoked()).isTrue();
  }

  @Test(description = "GITHUB-3066")
  public void ensureCanWireInCustomExecutorServiceWhenEnabledViaAPIForMultipleSuites() {
    XmlSuite xmlSuite1 = createXmlSuite("suite1", "test1", TestClassSample.class);
    XmlSuite xmlSuite2 = createXmlSuite("suite2", "test2", TestClassSample.class);
    TestNG testng = create();
    testng.setXmlSuites(List.of(xmlSuite1, xmlSuite2));
    testng.setSuiteThreadPoolSize(2);
    testng.setExecutorServiceFactory(new Issue3066ExecutorServiceFactory());
    testng.run();
    assertThat(Issue3066ThreadPoolExecutor.isInvoked()).isTrue();
  }

  @AfterMethod
  public void resetState() {
    Issue3066ThreadPoolExecutor.resetInvokedState();
  }
}
