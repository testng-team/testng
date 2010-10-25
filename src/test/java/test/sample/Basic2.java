package test.sample;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Basic2 {
  private boolean m_basic2WasRun = false;
  private static int m_afterClass = 0;

  @Test(dependsOnGroups = { "basic1" })
  public void basic2() {
    m_basic2WasRun = true;
    assert Basic1.getCount() > 0 : "COUNT WAS NOT INCREMENTED";
  }

  @AfterTest
  public void cleanUp() {
    m_basic2WasRun = false;
    m_afterClass = 0;
  }

  private void ppp(String s) {
    System.out.println("[Basic2 "
        + Thread.currentThread().getId() + " ] " + hashCode() + " " + s);
  }

  @AfterClass
  public void checkTestAtClassLevelWasRun() {
    m_afterClass++;
    assert m_basic2WasRun : "Class annotated with @Test didn't have its methods run.";
    assert 1 == m_afterClass : "After class should have been called exactly once, not " + m_afterClass;
  }
}