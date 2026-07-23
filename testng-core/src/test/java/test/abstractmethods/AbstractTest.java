package test.abstractmethods;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class AbstractTest extends SimpleBaseTest {

  @Test(description = "Abstract methods defined in a superclass should be run")
  public void abstractShouldRun() {
    TestNG tng = create(CRUDTest2.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertThat(tla.getPassedTests().size()).isEqualTo(2);
  }
}
