package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {"group1"})
public class ConfigurationInheritGroupsSampleTest {
  private boolean m_ok = false;

  @BeforeMethod
  public void setUp() {
    m_ok = true;
  }

  public void test1() {
    assertThat(m_ok).isTrue();
  }
}
