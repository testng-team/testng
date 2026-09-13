package org.testng.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.factory.samples.OrderFactory;
import org.testng.factory.samples.OrderSample;
import test.SimpleBaseTest;

public class FactoryOrderMainTest extends SimpleBaseTest {

  @Test
  public void factoriesShouldBeInvokedInTheOrderOfCreation() {
    TestNG tng = create(OrderFactory.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    List<ITestResult> passed = tla.getPassedTests();
    // Without this the loop below is empty when the factory produces nothing, and an empty loop
    // asserts nothing. A regression that stops the factory registering instances would pass.
    assertThat(passed).hasSize(5);
    for (int i = 0; i < passed.size(); i++) {
      assertThat(((OrderSample) passed.get(i).getInstance()).getValue()).isEqualTo(i);
    }
  }
}
