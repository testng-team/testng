package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ListenerInXmlTest extends SimpleBaseTest {

  @Test(description = "Make sure that listeners defined in testng.xml are invoked")
  public void listenerInXmlShouldBeInvoked() {
    TestNG tng = create();
    tng.setTestSuites(List.of(getPathToResource("listener-in-xml.xml")));
    LListener.invoked = false;
    tng.run();
    assertThat(LListener.invoked).isTrue();
  }
}
