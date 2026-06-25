package test.testng59;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** This class/interface */
public class Test2 {
  private boolean m_run = false;

  @Test
  public void test2() {
    m_run = true;
  }

  @AfterClass
  public void checkWasRun() {
    assertThat(m_run)
        .withFailMessage("test2() should have been run according to testng-59.xml")
        .isTrue();
  }
}
