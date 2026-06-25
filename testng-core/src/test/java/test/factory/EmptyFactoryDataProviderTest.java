package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class EmptyFactoryDataProviderTest extends SimpleBaseTest {

  @Test
  public void test() {
    TestNG testng = create(ArrayEmptyFactorySample.class, IteratorEmptyFactorySample.class);

    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    // Used to check the warning message

    testng.run();

    assertThat(tla.getFailedTests()).isEmpty();
    assertThat(tla.getSkippedTests()).isEmpty();
    assertThat(tla.getPassedTests()).isEmpty();
  }
}
