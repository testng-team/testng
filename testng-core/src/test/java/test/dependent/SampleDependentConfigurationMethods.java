package test.dependent;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleDependentConfigurationMethods {
  private boolean m_create = false;
  private boolean m_first = false;

  @BeforeMethod
  public void createInstance() {
    m_create = true;
  }

  @BeforeMethod(dependsOnMethods = {"createInstance"})
  public void firstInvocation() {
    assertTrue(m_create, "createInstance() was never called");
    m_first = true;
  }

  @Test
  public void verifyDependents() {
    assertTrue(m_create, "createInstance() was never called");
    assertTrue(m_first, "firstInvocation() was never called");
  }
}
