package test.guice;

import com.google.inject.Inject;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Test(guiceModule = GuiceExampleModule.class)
public class GuiceTest extends SimpleBaseTest {

  @Inject
  ISingleton m_singleton;

  @Test
  public void singletonShouldWork() {
    m_singleton.doSomething();
  }

  @Test(expectedExceptions = TestNGException.class)
  public void invalidModuleShouldFail() {
    TestNG tng = create(GuiceShouldFailTest.class);
    tng.run();
  }
}
