package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Test(groups = "foo")
public class MultipleBeforeGroupTest {
  private int m_count = 0;

  @BeforeGroups("foo")
  public void beforeGroups() {
    m_count++;
  }

  @Test()
  public void test() {}

  @Test(dependsOnMethods = "test")
  public void verify() {
    assertThat(m_count).isOne();
  }
}
