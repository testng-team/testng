package test.cli.testpackage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.cli.CliParseException;
import org.testng.cli.jcommander.JCommanderCliRunner;
import org.testng.testhelper.OutputDirectoryPatch;
import test.SimpleBaseTest;

/** GITHUB-1226. {@code -testpackage} selects test classes by package, like {@code <package>}. */
public class TestPackageCommandLineTest extends SimpleBaseTest {

  private static final String TEST_PACKAGE = "-testpackage";
  private static final String A = "test.cli.testpackage.a";
  private static final String B = "test.cli.testpackage.b";
  private static final String C = "test.cli.testpackage.c";

  @Test(description = "GITHUB-1226")
  public void runsTheClassesOfThePackageButNotOfItsSubPackages() {
    assertThat(run(TEST_PACKAGE, A)).containsExactly("firstTest");
  }

  @Test(description = "GITHUB-1226")
  public void aTrailingWildcardAlsoRunsTheSubPackages() {
    assertThat(run(TEST_PACKAGE, A + ".*")).containsExactlyInAnyOrder("firstTest", "nestedTest");
  }

  @Test(description = "GITHUB-1226")
  public void acceptsACommaSeparatedListOfPackages() {
    assertThat(run(TEST_PACKAGE, A + "," + B)).containsExactlyInAnyOrder("firstTest", "otherTest");
  }

  @Test(description = "GITHUB-1226")
  public void combinesWithTestClass() {
    assertThat(run(TEST_PACKAGE, A, "-testclass", B + ".OtherSample"))
        .containsExactlyInAnyOrder("firstTest", "otherTest");
  }

  @Test(description = "GITHUB-1226")
  public void aClassThatTestClassPutsInItsOwnTestRunsOnce() {
    assertThat(run(TEST_PACKAGE, C, "-testclass", C + ".NamedSample")).containsExactly("namedTest");
  }

  @Test(description = "GITHUB-1226")
  public void groupsFilterThePackagesWithoutATestClass() {
    assertThat(run(TEST_PACKAGE, A + ".*", "-groups", "nested")).containsExactly("nestedTest");
  }

  @Test(description = "GITHUB-1226")
  public void aPackageListThatNamesNoPackageSelectsNothing() {
    assertThatThrownBy(() -> run(TEST_PACKAGE, " , "))
        .isInstanceOf(CliParseException.class)
        .hasMessageContaining("at least one");
  }

  @Test(description = "GITHUB-1226")
  public void theJavaApiRunsThePackages() {
    TestNG testng = create();
    testng.setTestPackages(Collections.singletonList(A + ".*"));
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);

    testng.run();

    assertThat(names(listener.getPassedTests()))
        .containsExactlyInAnyOrder("firstTest", "nestedTest");
  }

  private static List<String> run(String... options) {
    List<String> args =
        new ArrayList<>(
            Arrays.asList("-log", "0", "-d", OutputDirectoryPatch.getOutputDirectory()));
    args.addAll(Arrays.asList(options));
    TestListenerAdapter listener = new TestListenerAdapter();
    new JCommanderCliRunner().run(args.toArray(new String[0]), listener);
    assertThat(listener.getFailedTests()).isEmpty();
    return names(listener.getPassedTests());
  }

  private static List<String> names(List<ITestResult> results) {
    List<String> names = new ArrayList<>();
    for (ITestResult result : results) {
      names.add(result.getMethod().getMethodName());
    }
    return names;
  }
}
