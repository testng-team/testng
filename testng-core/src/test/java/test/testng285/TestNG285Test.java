package test.testng285;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.BaseTest;

public class TestNG285Test extends BaseTest {

  @Test
  public void verifyBug() {
    addClass("test.testng285.Derived");
    setParallel(XmlSuite.ParallelMode.METHODS);
    setThreadCount(5);

    run();

    assertThat(BugBase.m_threadIds.size()).isEqualTo(1);
  }
}
