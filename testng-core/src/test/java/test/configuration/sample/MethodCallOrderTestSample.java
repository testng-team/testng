package test.configuration.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MethodCallOrderTestSample {
  public static boolean s_beforeSuite;
  public static boolean s_beforeTest;
  public static boolean s_beforeClass;
  public static boolean s_beforeMethod;

  @BeforeClass
  public void beforeClass() {
    assertThat(s_beforeSuite).isTrue();
    assertThat(s_beforeTest).isTrue();
    assertThat(s_beforeClass).isFalse();
    assertThat(s_beforeMethod).isFalse();

    s_beforeClass = true;
  }

  @AfterSuite
  public void cleanUp() {
    s_beforeSuite = false;
    s_beforeTest = false;
    s_beforeClass = false;
    s_beforeMethod = false;
  }

  @BeforeMethod
  public void beforeMethod() {
    assertThat(s_beforeSuite).isTrue();
    assertThat(s_beforeTest).isTrue();
    assertThat(s_beforeClass).isTrue();
    assertThat(s_beforeMethod).isFalse();
    s_beforeMethod = true;
  }

  @Test
  public void realTest() {
    assertThat(s_beforeSuite).isTrue();
    assertThat(s_beforeTest).isTrue();
    assertThat(s_beforeClass).isTrue();
    assertThat(s_beforeMethod).isTrue();
  }

  @AfterMethod
  public void afterMethod() {
    assertThat(ExternalConfigurationClassSample.s_afterMethod)
        .withFailMessage("afterTestMethod shouldn't have been run")
        .isFalse();
    assertThat(ExternalConfigurationClassSample.s_afterClass)
        .withFailMessage("afterTestClass shouldn't have been run")
        .isFalse();
    assertThat(ExternalConfigurationClassSample.s_afterTest)
        .withFailMessage("afterTest should haven't been run")
        .isFalse();

    ExternalConfigurationClassSample.s_afterMethod = true;
  }

  @AfterClass
  public void afterClass() {
    assertThat(ExternalConfigurationClassSample.s_afterMethod)
        .withFailMessage("afterTestMethod should have been run")
        .isTrue();
    assertThat(ExternalConfigurationClassSample.s_afterClass)
        .withFailMessage("afterTestClass shouldn't have been run")
        .isFalse();
    assertThat(ExternalConfigurationClassSample.s_afterTest)
        .withFailMessage("afterTest should haven't been run")
        .isFalse();
    ExternalConfigurationClassSample.s_afterClass = true;
  }
}
