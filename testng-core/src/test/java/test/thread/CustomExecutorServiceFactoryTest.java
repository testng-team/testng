package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.thread.issue3066.Issue3066ExecutorServiceFactory;
import test.thread.issue3066.Issue3066ThreadPoolExecutor;
import test.thread.issue3066.TestClassSample;

public class CustomExecutorServiceFactoryTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3066")
  public void ensureCanWireInCustomExecutorServiceWhenEnabledViaConfigParam() {
    String[] args = {
      "-testclass",
      TestClassSample.class.getName(),
      "-threadpoolfactoryclass",
      Issue3066ExecutorServiceFactory.class.getName(),
      "-parallel",
      "methods"
    };
    TestNG.privateMain(args, null);
    assertThat(Issue3066ThreadPoolExecutor.isInvoked()).isTrue();
  }

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

  @Test(description = "GITHUB-3066")
  public void ensureCanWireInCustomExecutorServiceWhenEnabledViaConfigForMultipleSuites() {
    AtomicInteger counter = new AtomicInteger(1);
    List<String> suites = new ArrayList<>();
    File dir = createDirInTempDir("suites");
    Stream.of(TestClassSample.class, TestClassSample.class)
        .map(
            it -> createXmlSuite("suite-" + counter.get(), "test-" + counter.getAndIncrement(), it))
        .map(XmlSuite::toXml)
        .forEach(
            it -> {
              Path s1 = Paths.get(dir.getAbsolutePath(), UUID.randomUUID() + "-suite.xml");
              try {
                Files.writeString(s1, it);
                suites.add(s1.toFile().getAbsolutePath());
              } catch (IOException ignored) {
              }
            });

    List<String> args =
        List.of(
            "-threadpoolfactoryclass",
            Issue3066ExecutorServiceFactory.class.getName(),
            "-suitethreadpoolsize",
            "2");
    TestNG.privateMain(Lists.merge(suites, args).toArray(String[]::new), null);
    assertThat(Issue3066ThreadPoolExecutor.isInvoked()).isTrue();
  }

  @AfterMethod
  public void resetState() {
    Issue3066ThreadPoolExecutor.resetInvokedState();
  }
}
