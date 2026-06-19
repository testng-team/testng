package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Basic2 {
  private boolean m_basic2WasRun = false;
  private static int m_afterClass = 0;

  @Test(dependsOnGroups = {"basic1"})
  public void basic2() {
    m_basic2WasRun = true;
    assertThat(Basic1.getCount() > 0).withFailMessage("COUNT WAS NOT INCREMENTED").isTrue();
  }

  @AfterTest
  public void cleanUp() {
    m_basic2WasRun = false;
    m_afterClass = 0;
  }

  @AfterClass
  public void checkTestAtClassLevelWasRun() {
    m_afterClass++;
    assertThat(m_basic2WasRun)
        .withFailMessage("Class annotated with @Test didn't have its methods run.")
        .isTrue();
    assertThat(m_afterClass)
        .withFailMessage("After class should have been called exactly once, not " + m_afterClass)
        .isEqualTo(1);
  }
}
