package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests groups that are partially defined at the class level and then augmented at the
 * method level.
 *
 * @author cbeust
 */
@Test(groups = {"classGroup"})
public class PartialGroupTest {
  public static boolean m_successMethod = false;
  public static boolean m_successClass = false;

  @BeforeClass
  public void init() {
    m_successMethod = false;
    m_successClass = false;
  }

  @Test(groups = {"methodGroup"})
  public void testMethodGroup() {
    m_successMethod = true;
  }

  @Test
  public void testClassGroupShouldFail() {
    assertThat(false).isTrue();
  }

  @Test(groups = {"methodGroup"})
  public void testMethodGroupShouldFail() {
    assertThat(false).isTrue();
  }

  @Test
  public void testClassGroup() {
    m_successClass = true;
  }
}
