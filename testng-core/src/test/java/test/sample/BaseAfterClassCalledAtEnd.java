package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;

public class BaseAfterClassCalledAtEnd {
  protected boolean m_afterClass = false;

  @AfterClass(dependsOnGroups = {".*"})
  public void baseAfterClass() {
    assertThat(m_afterClass)
        .withFailMessage("This afterClass method should have been called last")
        .isTrue();
  }
}
