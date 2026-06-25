package test.uniquesuite;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class TestBefore2 extends BaseBefore {

  @Test
  public void verify() {
    assertThat(m_beforeCount).isOne();
  }
}
