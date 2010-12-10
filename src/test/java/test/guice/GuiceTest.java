package test.guice;

import com.google.inject.Inject;

import org.testng.annotations.Test;

@Test(guiceModule = GuiceExampleModule.class)
public class GuiceTest {

  @Inject
  ISingleton m_singleton;

  @Test
  public void singletonShouldWork() {
    m_singleton.doSomething();
  }
}
