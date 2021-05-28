package test.listeners;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import test.sample.Sample1;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultEndMillisTest extends SimpleBaseTest {

  @Test
  public void endMillisShouldBeNonNull() {
    TestNG tng = create(Sample1.class);
    tng.addListener((ITestNGListener) new ResultListener());
    tng.run();

    assertThat(ResultListener.m_end > 0).isTrue();
  }
}
