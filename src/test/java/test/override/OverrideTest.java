package test.override;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.Utils;
import org.xml.sax.SAXException;

import test.SimpleBaseTest;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class OverrideTest extends SimpleBaseTest {

  @Test(description = "Verify that groups specified on the command line override tests in " +
  		"testng.xml")
  public void overrideShouldWork() throws ParserConfigurationException, SAXException, IOException {
    File f = Utils.createTempFile(
        "<suite name=\"S\">"
        + "  <test name=\"T\">"
        + "    <groups>"
        + "      <run>"
        + "        <include name=\"badGroup\" />"
        + "       </run>"
        + "    </groups>"
        + ""
        + "    <classes>"
        + "      <class name=\"test.override.OverrideSampleTest\" />"
        + "    </classes>"
        + "  </test>"
        + "</suite>"
        );
    TestNG tng = create();
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.setGroups("goodGroup");
    tng.setTestSuites(Arrays.asList(f.getAbsolutePath()));
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }
}
