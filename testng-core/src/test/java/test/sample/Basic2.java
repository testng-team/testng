package test.sample;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Basic2 {
  private boolean m_basic2WasRun = false;
  private static int m_afterClass = 0;

  @Test(dependsOnGroups = {"basic1"})
  public void basic2() {
    m_basic2WasRun = true;
    assertTrue(Basic1.getCount() > 0, "COUNT WAS NOT INCREMENTED");
  }

  @AfterTest
  public void cleanUp() {
    m_basic2WasRun = false;
    m_afterClass = 0;
  }

  @AfterClass
  public void checkTestAtClassLevelWasRun() {
    m_afterClass++;
    assertTrue(m_basic2WasRun, "Class annotated with @Test didn't have its methods run.");
    assertEquals(
        m_afterClass, 1, "After class should have been called exactly once, not " + m_afterClass);
  }
}
