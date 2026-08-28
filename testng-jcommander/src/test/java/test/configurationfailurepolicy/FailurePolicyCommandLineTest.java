package test.configurationfailurepolicy;

import org.testng.TestListenerAdapter;
import org.testng.annotations.Test;
import org.testng.cli.jcommander.JCommanderCliRunner;
import org.testng.testhelper.OutputDirectoryPatch;
import test.SimpleBaseTest;
import test.TestHelper;

/**
 * The command line half of {@code test.configurationfailurepolicy.FailurePolicyTest}, which stays
 * in {@code testng-core} for the cases that drive the Java API. These are the only tests covering
 * the {@code -configfailurepolicy} string to {@code XmlSuite.FailurePolicy} conversion.
 */
public class FailurePolicyCommandLineTest extends SimpleBaseTest {

  @Test
  public void commandLineTest_policyAsSkip() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "skip",
          "-testclass",
          ClassWithFailedBeforeMethodAndMultipleTests.class.getCanonicalName()
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    new JCommanderCliRunner().run(argv, tla);

    TestHelper.assertCounts(tla, 1, 1, 2);
  }

  @Test
  public void commandLineTest_policyAsContinue() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "continue",
          "-testclass",
          ClassWithFailedBeforeMethodAndMultipleTests.class.getCanonicalName()
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    new JCommanderCliRunner().run(argv, tla);

    TestHelper.assertCounts(tla, 2, 0, 2);
  }

  @Test
  public void commandLineTestWithXMLFile_policyAsSkip() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "skip",
          getPathToResource("testng-configfailure.xml")
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    new JCommanderCliRunner().run(argv, tla);

    TestHelper.assertCounts(tla, 1, 1, 2);
  }

  @Test
  public void commandLineTestWithXMLFile_policyAsContinue() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "continue",
          getPathToResource("testng-configfailure.xml")
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    new JCommanderCliRunner().run(argv, tla);

    TestHelper.assertCounts(tla, 2, 0, 2);
  }
}
