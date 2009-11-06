package test.factory;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class FactoryInSuperClassTest extends SimpleBaseTest {

  @Test
  public void factoryInSuperClassShouldWork() {
    TestNG tng = create(FactoryChild.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }
}
