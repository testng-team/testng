package test.configuration;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class BeforeClassThreadTest extends SimpleBaseTest {

  @Test
  public void beforeClassMethodsShouldRunInParallel() {
    TestNG tng = create(BeforeClassThreadA.class, BeforeClassThreadB.class);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    tng.run();
    assertThat(Math.abs(BeforeClassThreadA.WHEN - BeforeClassThreadB.WHEN)).isLessThan(1000);
  }
}
