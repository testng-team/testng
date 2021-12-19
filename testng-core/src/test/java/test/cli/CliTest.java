package test.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.function.Supplier;
import org.testng.Assert;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.cli.github1517.TestClassWithConfigFailureSample;
import test.cli.github1517.TestClassWithConfigSkipAndFailureSample;
import test.cli.github1517.TestClassWithConfigSkipSample;
import test.cli.github2693.TestClassSample;

public class CliTest extends SimpleBaseTest {
  @BeforeMethod
  public void clearThreadsInformation() {
    TestClassSample.threads.clear();
  }

  @Test(dataProvider = "getScenarios", description = "GITHUB-2693")
  public void ensureDataProviderCountCanBeOverridden(String scenario, CommandLineArgs args) {
    CustomTestNG tng = new CustomTestNG();
    tng.configure(args);
    tng.run();
    assertThat(TestClassSample.threads).withFailMessage(scenario).hasSize(2);
  }

  @DataProvider(name = "getScenarios")
  public Object[][] getScenarios() {
    Supplier<CommandLineArgs> supplier =
        () -> {
          CommandLineArgs cli = new CommandLineArgs();
          cli.dataProviderThreadCount = 2;
          return cli;
        };
    CommandLineArgs cliSuites = supplier.get();
    cliSuites.suiteFiles = Collections.singletonList("src/test/resources/xml/issue2693.xml");
    String className = TestClassSample.class.getName();

    CommandLineArgs cliClasses = supplier.get();
    cliClasses.testClass = className;

    CommandLineArgs cliMethods = supplier.get();
    cliMethods.commandLineMethods = Collections.singletonList(className + ".test");
    return new Object[][] {
      new Object[] {"CLI With Suites", cliSuites},
      new Object[] {"CLI With Classes", cliClasses},
      new Object[] {"CLI With Methods", cliMethods}
    };
  }

  @Test(dataProvider = "getData", description = "GITHUB-1517")
  public void testExitCodeListenerBehavior(Class<?> clazz, int expectedStatus) {
    TestNG testNG = create(clazz);
    testNG.run();
    Assert.assertEquals(testNG.getStatus(), expectedStatus);
  }

  @DataProvider
  public Object[][] getData() {
    return new Object[][] {
      {TestClassWithConfigFailureSample.class, 3},
      {TestClassWithConfigSkipSample.class, 2},
      {TestClassWithConfigSkipAndFailureSample.class, 3}
    };
  }

  public static class CustomTestNG extends TestNG {

    @Override
    public void configure(CommandLineArgs cla) {
      super.configure(cla);
    }
  }
}
