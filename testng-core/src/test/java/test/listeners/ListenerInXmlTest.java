package test.listeners;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ListenerInXmlTest extends SimpleBaseTest {

  @Test(description = "Make sure that listeners defined in testng.xml are invoked")
  public void listenerInXmlShouldBeInvoked() {
    TestNG tng = create();
    tng.setTestSuites(Arrays.asList(getPathToResource("listener-in-xml.xml")));
    LListener.invoked = false;
    tng.run();
    Assert.assertTrue(LListener.invoked);
  }
}
