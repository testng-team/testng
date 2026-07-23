package test.listeners.github1284;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class TestListeners extends SimpleBaseTest {

  @Test(priority = 1)
  public void verifyWithoutListener() {
    TestNG testNG = create();
    testNG.setTestSuites(
        Collections.singletonList(
            getPathToResource("test/listeners/github1284/github1284_nolistener.xml")));
    testNG.run();
    assertThat(testNG.getStatus()).isEqualTo(0);
  }

  @Test(priority = 2)
  public void verifyWithListener() {
    TestNG testNG = create();
    testNG.setTestSuites(
        Collections.singletonList(
            getPathToResource("test/listeners/github1284/github1284_withlistener.xml")));
    testNG.run();
    assertThat(testNG.getStatus()).isEqualTo(0);
  }

  @Test(priority = 3)
  public void verifyWithChildSuite() {
    TestNG testNG = create();
    testNG.setTestSuites(
        Collections.singletonList(getPathToResource("test/listeners/github1284/github1284.xml")));
    testNG.run();
    assertThat(testNG.getStatus()).isEqualTo(0);
  }
}
