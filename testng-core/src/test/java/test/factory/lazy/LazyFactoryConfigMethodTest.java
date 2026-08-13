package test.factory.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * Verifies that a suite level ({@code @BeforeSuite}) or {@code <test>} level ({@code @BeforeTest})
 * configuration method on a factory-powered class behaves correctly under lazy instantiation: the
 * configuration fires exactly once, every instance is still constructed exactly once, every test
 * runs, and laziness is preserved — only the single instance the configuration runs on is
 * instantiated early, the rest are still built just-in-time.
 */
public class LazyFactoryConfigMethodTest extends SimpleBaseTest {

  @Test
  public void beforeSuiteOnLazyFactory() {
    BeforeSuiteFactorySample.reset();
    TestNG tng = create(BeforeSuiteFactorySample.class);
    tng.setPreserveOrder(true);
    tng.setLazyFactoryInstantiation(true);
    tng.run();

    // @BeforeSuite fires exactly once for the whole factory (it is not multiplied per instance).
    assertThat(BeforeSuiteFactorySample.BEFORE_SUITE_FIRED).hasValue(1);
    // Every factory instance is constructed exactly once and every test runs.
    assertThat(BeforeSuiteFactorySample.CONSTRUCTED).hasValue(4);
    assertThat(BeforeSuiteFactorySample.TESTS_RUN).containsExactlyInAnyOrder(0, 1, 2, 3);
    // Laziness is preserved: the configuration instantiates only the one instance it runs on, so
    // fewer than all four instances existed when the first test ran (eager would have all four).
    assertThat(BeforeSuiteFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.get(0)).isLessThan(4);
  }

  @Test
  public void beforeTestOnLazyFactory() {
    BeforeTestFactorySample.reset();
    TestNG tng = create(BeforeTestFactorySample.class);
    tng.setPreserveOrder(true);
    tng.setLazyFactoryInstantiation(true);
    tng.run();

    // @BeforeTest fires exactly once for the whole factory (it is not multiplied per instance).
    assertThat(BeforeTestFactorySample.BEFORE_TEST_FIRED).hasValue(1);
    // Every factory instance is constructed exactly once and every test runs.
    assertThat(BeforeTestFactorySample.CONSTRUCTED).hasValue(4);
    assertThat(BeforeTestFactorySample.TESTS_RUN).containsExactlyInAnyOrder(0, 1, 2, 3);
    // Laziness is preserved: the configuration instantiates only the one instance it runs on, so
    // fewer than all four instances existed when the first test ran (eager would have all four).
    assertThat(BeforeTestFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN.get(0)).isLessThan(4);
  }
}
