package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.cli.jcommander.JCommanderCliRunner;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.thread.issue3066.Issue3066ExecutorServiceFactory;
import test.thread.issue3066.Issue3066ThreadPoolExecutor;
import test.thread.issue3066.TestClassSample;

/**
 * The command line half of {@code test.thread.CustomExecutorServiceFactoryTest}, which stays in
 * {@code testng-core} for the cases that drive the Java API. These are the only tests covering the
 * {@code -threadpoolfactoryclass} and {@code -suitethreadpoolsize} options end to end.
 */
public class CustomExecutorServiceFactoryCommandLineTest extends SimpleBaseTest {

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
    new JCommanderCliRunner().run(args, null);
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
              } catch (IOException e) {
                // Swallowing this would quietly turn a two-suite test into a one-suite one.
                throw new UncheckedIOException(e);
              }
            });

    List<String> args =
        List.of(
            "-threadpoolfactoryclass",
            Issue3066ExecutorServiceFactory.class.getName(),
            "-suitethreadpoolsize",
            "2");
    new JCommanderCliRunner().run(Lists.merge(suites, args).toArray(String[]::new), null);
    assertThat(Issue3066ThreadPoolExecutor.isInvoked()).isTrue();
  }

  @AfterMethod
  public void resetState() {
    Issue3066ThreadPoolExecutor.resetInvokedState();
  }
}
