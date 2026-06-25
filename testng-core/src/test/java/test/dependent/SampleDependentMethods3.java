package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class tests overloaded dependent methods
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class SampleDependentMethods3 {
  private boolean m_oneA = false;
  private boolean m_oneB = false;
  private boolean m_secondA = false;

  @Test
  public void one() {
    assertThat(m_secondA).withFailMessage("secondA shouldn't have been run yet").isFalse();
    m_oneA = true;
  }

  @Parameters({"foo"})
  @Test
  public void one(String s) {
    assertThat(m_secondA).withFailMessage("secondA shouldn't have been run yet").isFalse();
    assertThat(s)
        .withFailMessage("Expected parameter value Cedric but got " + s)
        .isEqualTo("Cedric");
    m_oneB = true;
  }

  @Test(dependsOnMethods = {"one"})
  public void secondA() {
    assertThat(m_oneA).withFailMessage("oneA wasn't run").isTrue();
    assertThat(m_oneB).withFailMessage("oneB wasn't run").isTrue();
    assertThat(m_secondA).withFailMessage("secondA shouldn't have been run yet").isFalse();
    m_secondA = true;
  }

  @AfterClass
  public void tearDown() {
    assertThat(m_oneA).withFailMessage("oneA wasn't run").isTrue();
    assertThat(m_oneB).withFailMessage("oneB wasn't run").isTrue();
    assertThat(m_secondA).withFailMessage("secondA wasn't run").isTrue();
  }
}
