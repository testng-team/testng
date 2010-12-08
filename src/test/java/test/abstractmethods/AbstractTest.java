package test.abstractmethods;

import org.testng.Assert;
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

    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }
}
