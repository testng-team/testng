package test.commandline;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.cli.jcommander.JCommanderCliRunner;
import org.testng.testhelper.JarCreator;
import test.SimpleBaseTest;
import test.TestHelper;
import test.commandline.issue341.LocalLogAggregator;
import test.commandline.issue341.TestSampleA;
import test.commandline.issue341.TestSampleB;

/**
 * The command line half of {@code test.commandline.CommandLineOverridesXml}, which stays in {@code
 * testng-core} for the cases that drive the Java API.
 */
public class CommandLineOverridesXmlCommandLineTest extends SimpleBaseTest {

  @Test(description = "GITHUB-341")
  public void ensureParallelismIsHonoredWhenOnlyClassesSpecifiedInJar() throws IOException {
    LocalLogAggregator.clearLogs();
    Class<?>[] classes = new Class<?>[] {TestSampleA.class, TestSampleB.class};
    File jarfile = JarCreator.generateJar(classes);
    String[] args =
        new String[] {
          "-parallel",
          "classes",
          "-testjar",
          jarfile.getAbsolutePath(),
          "-listener",
          LocalLogAggregator.class.getCanonicalName()
        };
    new JCommanderCliRunner().run(args, null);
    Set<String> logs = LocalLogAggregator.getLogs();
    assertThat(logs).hasSize(2);
  }

  @Test(description = "GITHUB-1810")
  public void ensureNoNullPointerExceptionIsThrown() throws IOException {
    String[] args = {
      TestHelper.writeSuiteToTempFile(buildSuiteContentThatRefersToInvalidTestClass())
    };
    TestNG testng = new JCommanderCliRunner().run(args, null);
    assertThat(testng.getStatus()).isEqualTo(8);
  }

  private static String buildSuiteContentThatRefersToInvalidTestClass() {
    return TestHelper.SUITE_XML_HEADER
        + "<suite name=\"1810_Suite\">\n"
        + "    <test name=\"1810_test\">\n"
        + "        <classes>\n"
        + "            <class name=\"com.foo.bar.issue1810.ClassDoesnotExist\">\n"
        + "            </class>\n"
        + "        </classes>\n"
        + "    </test>\n"
        + "</suite>\n";
  }
}
