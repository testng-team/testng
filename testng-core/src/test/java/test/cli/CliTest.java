package test.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.testng.Assert;
import org.testng.CommandLineArgs;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.testhelper.CompiledCode;
import org.testng.testhelper.JarCreator;
import org.testng.testhelper.SimpleCompiler;
import org.testng.testhelper.SourceCode;
import org.testng.testhelper.TestClassGenerator;
import org.testng.testhelper.TestNGSimpleClassLoader;
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

  @Test(description = "GITHUB-2761")
  public void testToEnsureSuitesInJarAreExecutedViaCli() throws IOException {
    SourceCode[] sources =
        TestClassGenerator.generate("com.kungfu.panda", Arrays.asList("DragonWarrior", "Tigress"));
    List<CompiledCode> compiledSources = SimpleCompiler.compileSourceCode(sources);
    TestNGSimpleClassLoader classLoader =
        new TestNGSimpleClassLoader(TestClassGenerator.getProjectDir());
    Class<?>[] classes =
        compiledSources.stream()
            .map(compiledCode -> compile(classLoader, compiledCode))
            .toArray(size -> new Class<?>[size]);
    File jar = JarCreator.generateJar(classes);
    LogInvocations logInvocations = new LogInvocations();
    TestNG testng = new TestNG();
    testng.addClassLoader(classLoader);
    testng.setTestJar(jar.getAbsolutePath());
    testng.addListener(logInvocations);
    testng.setVerbose(2);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    assertThat(logInvocations.logs)
        .containsExactlyInAnyOrder(
            "com.kungfu.panda.DragonWarrior.testMethod", "com.kungfu.panda.Tigress.testMethod");
  }

  private static Class<?> compile(TestNGSimpleClassLoader loader, CompiledCode code) {
    try {
      return loader.injectByteCode(code);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static class LogInvocations implements IInvokedMethodListener {
    private final List<String> logs = new ArrayList<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      logs.add(method.getTestMethod().getQualifiedName());
    }

    public List<String> getLogs() {
      return logs;
    }
  }

  public static class CustomTestNG extends TestNG {

    @Override
    public void configure(CommandLineArgs cla) {
      super.configure(cla);
    }
  }
}
