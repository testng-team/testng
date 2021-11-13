package test.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class FactoryIntegrationTest extends SimpleBaseTest {

  @Test(description = "https://github.com/cbeust/testng/issues/876")
  public void testExceptionWithNonStaticFactoryMethod() {
    TestNG tng = create(GitHub876Sample.class);
    try {
      tng.run();
      failBecauseExceptionWasNotThrown(TestNGException.class);
    } catch (TestNGException e) {
      assertThat(e)
          .hasMessage(
              "\nCan't invoke public java.lang.Object[] test.factory.GitHub876Sample.createInstances(): either make it static or add a no-args constructor to your class");
    }
  }

  @Test
  public void testNonPublicFactoryMethodShouldWork() {
    TestNG tng = create(NonPublicFactory.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }

  @Test
  public void testExceptionWithBadFactoryMethodReturnType() {
    TestNG tng = create(BadMethodReturnTypeFactory.class);
    try {
      tng.run();
      failBecauseExceptionWasNotThrown(TestNGException.class);
    } catch (TestNGException e) {
      assertThat(e)
          .hasMessage(
              "\ntest.factory.BadMethodReturnTypeFactory.createInstances MUST return [ java.lang.Object[] or org.testng.IInstanceInfo[] ] but returns java.lang.Object");
    }
  }

  @Test
  public void doubleFactoryMethodShouldWork() {
    TestNG tng = create(DoubleFactory.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    // TODO containsExactly is not used here because the order is not consistent. Check if we should
    // fix it.
    assertThat(listener.getSucceedMethodNames())
        .contains(
            "FactoryBaseSample{1}#f",
            "FactoryBaseSample{2}#f", "FactoryBaseSample{3}#f", "FactoryBaseSample{4}#f");
  }
}
