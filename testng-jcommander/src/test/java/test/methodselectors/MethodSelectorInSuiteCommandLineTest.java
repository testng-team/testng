package test.methodselectors;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.testhelper.OutputDirectoryPatch;
import test.SimpleBaseTest;
import test.TestHelper;

/**
 * The command line half of {@code test.methodselectors.MethodSelectorInSuiteTest}, which stays in
 * {@code testng-core} for the cases that drive the Java API.
 */
public class MethodSelectorInSuiteCommandLineTest extends SimpleBaseTest {

  private TestListenerAdapter m_tla;

  @BeforeMethod
  public void setup() {
    m_tla = new TestListenerAdapter();
  }

  @Test
  public void fileOnCommandLine() {
    String[] args =
        new String[] {
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          getPathToResource("methodselector-in-xml.xml")
        };
    TestNG.privateMain(args, m_tla);

    TestHelper.assertPassedTestNames(m_tla.getPassedTests(), "test2");
  }
}
