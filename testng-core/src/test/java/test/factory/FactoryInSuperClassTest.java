package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class FactoryInSuperClassTest extends SimpleBaseTest {

  @Test
  public void factoryInSuperClassShouldWork() {
    TestNG tng = create(ChildFactory.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertThat(tla.getPassedTests()).hasSize(1);
  }
}
