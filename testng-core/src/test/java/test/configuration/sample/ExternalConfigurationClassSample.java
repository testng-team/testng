package test.configuration.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public class ExternalConfigurationClassSample {
  public static boolean s_afterMethod;
  public static boolean s_afterClass;
  public static boolean s_afterTest;

  @BeforeSuite
  public void beforeSuite() {
    MethodCallOrderTestSample.s_beforeSuite = true;
  }

  @AfterSuite
  public void cleanUp() {
    s_afterMethod = false;
    s_afterClass = false;
    s_afterTest = false;
  }

  @BeforeTest
  public void beforeTest() {
    assertThat(MethodCallOrderTestSample.s_beforeSuite).isTrue();
    assertThat(MethodCallOrderTestSample.s_beforeTest).isFalse();
    assertThat(MethodCallOrderTestSample.s_beforeClass).isFalse();
    assertThat(MethodCallOrderTestSample.s_beforeMethod).isFalse();

    MethodCallOrderTestSample.s_beforeTest = true;
  }

  @AfterTest
  public void afterTest() {
    assertThat(s_afterMethod).withFailMessage("afterTestMethod should have been run").isTrue();
    assertThat(s_afterClass).withFailMessage("afterTestClass should have been run").isTrue();
    assertThat(s_afterTest).withFailMessage("afterTest should haven't been run").isFalse();
    s_afterTest = true;
  }

  @AfterSuite
  public void afterSuite() {
    assertThat(s_afterMethod).withFailMessage("afterTestMethod should have been run").isTrue();
    assertThat(s_afterClass).withFailMessage("afterTestClass should have been run").isTrue();
    assertThat(s_afterTest).withFailMessage("afterTest should have been run").isTrue();
  }
}
