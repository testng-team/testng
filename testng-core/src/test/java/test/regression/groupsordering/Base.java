package test.regression.groupsordering;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

public abstract class Base {
  protected static boolean s_childAWasRun;
  protected static boolean s_childBWasRun;

  @BeforeGroups(value = "a", groups = "a")
  public void setUp() {
    assertThat(s_childAWasRun)
        .withFailMessage("Static field was not reset: @AfterGroup method not invoked")
        .isFalse();
    assertThat(s_childBWasRun)
        .withFailMessage("Static field was not reset: @AfterGroup method not invoked")
        .isFalse();
  }

  @AfterGroups(value = "a", groups = "a")
  public void tearDown() {
    assertThat(s_childAWasRun).withFailMessage("Child A was not run").isTrue();
    assertThat(s_childBWasRun).withFailMessage("Child B was not run").isTrue();
    s_childAWasRun = false;
    s_childBWasRun = false;
  }
}
