package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * This class exercises dependent methods
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class SampleDependentMethods {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;
  private boolean m_thirdA = false;

  @Test
  public void oneA() {
    assertThat(m_secondA).withFailMessage("secondA shouldn't have been run yet").isFalse();
    m_oneA = true;
  }

  @Test
  public void canBeRunAnytime() {}

  @Test(dependsOnMethods = {"oneA", "oneB"})
  public void secondA() {
    assertThat(m_oneA).withFailMessage("oneA wasn't run").isTrue();
    assertThat(m_oneB).withFailMessage("oneB wasn't run").isTrue();
    assertThat(m_secondA).withFailMessage("secondA shouldn't have been run yet").isFalse();
    m_secondA = true;
  }

  @Test(dependsOnMethods = {"secondA"})
  public void thirdA() {
    assertThat(m_oneA).withFailMessage("oneA wasn't run").isTrue();
    assertThat(m_oneB).withFailMessage("oneB wasn't run").isTrue();
    assertThat(m_secondA).withFailMessage("secondA wasn't run").isTrue();
    assertThat(m_thirdA).withFailMessage("thirdA shouldn't have been run yet").isFalse();
    m_thirdA = true;
  }

  @Test
  public void oneB() {
    assertThat(m_secondA).withFailMessage("secondA shouldn't have been run yet").isFalse();
    m_oneB = true;
  }

  @AfterClass
  public void tearDown() {
    assertThat(m_oneA).withFailMessage("oneA wasn't run").isTrue();
    assertThat(m_oneB).withFailMessage("oneB wasn't run").isTrue();
    assertThat(m_secondA).withFailMessage("secondA wasn't run").isTrue();
    assertThat(m_thirdA).withFailMessage("thirdA wasn't run").isTrue();
  }
}
