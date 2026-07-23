package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(m_create).withFailMessage("createInstance() was never called").isTrue();
    m_first = true;
  }

  @Test
  public void verifyDependents() {
    assertThat(m_create).withFailMessage("createInstance() was never called").isTrue();
    assertThat(m_first).withFailMessage("firstInvocation() was never called").isTrue();
  }
}
