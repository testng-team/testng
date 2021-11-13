package test.groups.issue1834;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import test.SimpleBaseTest;
import test.TestHelper;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1834")
  public void ensureDependenciesDefinedInSuiteAreHonored() throws IOException {
    File file = File.createTempFile("1834", ".xml");
    Files.writeFile(asSuite(), file);
    TestNG testng = create();
    testng.setTestSuites(Collections.singletonList(file.getAbsolutePath()));
    OutputGatheringListener listener = new OutputGatheringListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getConsoleLogs()).containsExactly("Uncached", "Cached");
  }

  private static String asSuite() {
    return TestHelper.SUITE_XML_HEADER
        + "<suite name=\"1834_Suite\" parallel=\"false\" verbose=\"0\">\n"
        + "  <test name=\"1834_Test\">\n"
        + "    <groups>\n"
        + "      <run>\n"
        + "        <include name=\"Uncached\"/>\n"
        + "        <include name=\"Cached\"/>\n"
        + "      </run>\n"
        + "      <dependencies>\n"
        + "        <group name=\"Cached\" depends-on=\"Uncached\"/>\n"
        + "      </dependencies>\n"
        + "    </groups>\n"
        + "    <classes>\n"
        + "      <class name=\""
        + TestSample.class.getName()
        + "\"/>\n"
        + "    </classes>\n"
        + "  </test>\n"
        + "</suite>";
  }
}
