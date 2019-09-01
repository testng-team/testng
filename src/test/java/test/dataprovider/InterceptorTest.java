package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.testng.reporters.Files;
import test.dataprovider.issue2111.CountingListener;
import test.dataprovider.issue2111.LocalDataProviderInterceptor;
import test.dataprovider.issue2111.TestClassExample;
import org.testng.IDataProviderInterceptor;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.dataprovider.issue2111.TestClassExamplePoweredByFactory;
import test.dataprovider.issue2111.TestClassExamplePoweredByFactoryUsingListener;
import test.dataprovider.issue2111.TestClassSampleUsingListener;

public class InterceptorTest extends SimpleBaseTest {

  @Test(dataProvider = "dp")
  public void ensureInterceptorIsInvokedForDataDriverPoweredTests(Class<?> testclass,
      IDataProviderInterceptor interceptor) {
    TestNG testng = create(testclass);
    if (interceptor != null) {
      testng.addListener(interceptor);
    }
    CountingListener counter = new CountingListener();
    testng.addListener(counter);
    testng.run();
    assertThat(counter.getResults()).hasSize(1);
  }

  @DataProvider(name = "dp")
  public Object[][] getTestData() {
    return new Object[][]{
        {TestClassExample.class, new LocalDataProviderInterceptor()},
        {TestClassSampleUsingListener.class, null},
        {TestClassExamplePoweredByFactory.class, new LocalDataProviderInterceptor()},
        {TestClassExamplePoweredByFactoryUsingListener.class, null}
    };
  }

  @Test(dataProvider = "dp1")
  public void ensureInterceptorIsInvokedViaListenersTag(Class<?> testClass) throws IOException {
    String xml = "<!DOCTYPE suite SYSTEM \"http://beust.com/testng/testng-1.0.dtd\">\n"
        + "<suite name=\"2111_suite\" verbose=\"2\">\n"
        + "    <listeners>\n"
        + "        <listener class-name=\"test.dataprovider.issue2111.LocalDataProviderInterceptor\"/>\n"
        + "        <listener class-name=\"test.dataprovider.issue2111.LocalDataProviderInterceptor\"/>\n"
        + "    </listeners>\n"
        + "    <test name=\"2111_test\">\n"
        + "        <classes>\n"
        + "            <class name=\"%s\"/>\n"
        + "        </classes>\n"
        + "    </test>\n"
        + "</suite>";
    xml = String.format(xml, testClass.getName());
    File suiteFile = File.createTempFile("testng", ".xml");
    Files.writeFile(xml, suiteFile);
    TestNG testng = create();
    testng.setTestSuites(Collections.singletonList(suiteFile.getAbsolutePath()));
    testng.setVerbose(2);
    CountingListener counter = new CountingListener();
    testng.addListener(counter);
    testng.run();
    assertThat(counter.getResults()).hasSize(1);
  }

  @DataProvider(name = "dp1")
  public Object[][] getTestClasses() {
    return new Object[][]{
        {TestClassExample.class},
        {TestClassExamplePoweredByFactory.class},
    };
  }


}
