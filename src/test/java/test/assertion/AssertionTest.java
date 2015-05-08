package test.assertion;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.LoggingAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertionTest {
  private LoggingAssert m_assert;
  private MyRawAssertion rawAssertion;

  @BeforeMethod
  public void bm() {
    m_assert = new LoggingAssert();
    rawAssertion = new MyRawAssertion();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test1() {
    m_assert.assertTrue(false, "new TestNG Assertion Failed");
  }

  @Test
  public void test2() {
    rawAssertion.assertTrue(true);
    rawAssertion.myAssert("test", true, "Raw test");

    assertThat(rawAssertion.getMethods())
        .containsExactly("onBeforeAssert", "onAssertSuccess", "onAfterAssert",
                         "onBeforeAssert", "onAssertSuccess", "onAfterAssert");
  }

  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Raw test .*")
  public void test2_fails() {
    try {
      rawAssertion.assertTrue(true);
      rawAssertion.myAssert("test", false, "Raw test");
    } catch (AssertionError error) {

      assertThat(rawAssertion.getMethods())
          .containsExactly("onBeforeAssert", "onAssertSuccess", "onAfterAssert",
                           "onBeforeAssert", "onAssertFailure", "deprecated_onAssertFailure", "onAfterAssert");

      throw error;
    }
  }
}
