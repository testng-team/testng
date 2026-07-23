package test.testng59;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** This class/interface */
public class Test1 {
  private boolean m_run = false;

  @Test
  public void test1() {
    m_run = true;
  }

  @AfterClass
  public void checkWasRun() {
    assertThat(m_run)
        .withFailMessage("test1() should have been run according to testng-59.xml")
        .isTrue();
  }
}
