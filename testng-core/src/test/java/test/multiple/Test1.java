package test.multiple;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Test1 {
  private static int m_count = 0;

  @Test
  public void f1() {
    assertThat(m_count < 1).withFailMessage("FAILING").isTrue();
    m_count++;
  }

  @AfterTest
  public void cleanUp() {
    m_count = 0;
  }
}
