package test.access;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test that private and protected @Configuration methods are run
 *
 * @author cbeust
 */
public class PrivateAccessConfigurationMethods extends BasePrivateAccessConfigurationMethods {
  private boolean m_private = false;
  private boolean m_default = false;
  private boolean m_protected = false;
  private boolean m_public = false;

  @BeforeMethod
  private void privateConfBeforeMethod() {
    m_private = true;
  }

  @BeforeMethod
  void defaultConfBeforeMethod() {
    m_default = true;
  }

  @BeforeMethod
  protected void protectedConfBeforeMethod() {
    m_protected = true;
  }

  @BeforeMethod
  public void publicConfBeforeMethod() {
    m_public = true;
  }

  @Test
  public void allAccessModifiersConfiguration() {
    assertThat(m_private).withFailMessage("private @Configuration should have been run").isTrue();
    assertThat(m_default).withFailMessage("default @Configuration should have been run").isTrue();
    assertThat(m_protected)
        .withFailMessage("protected @Configuration should have been run")
        .isTrue();
    assertThat(m_public).withFailMessage("public @Configuration should have been run").isTrue();

    assertThat(m_baseProtected)
        .withFailMessage("protected base @Configuration should have been run")
        .isTrue();
    assertThat(m_baseDefault)
        .withFailMessage("default base @Configuration should have been run")
        .isTrue();
    assertThat(m_basePrivate)
        .withFailMessage("private base @Configuration should not have been run")
        .isTrue();
  }
}
