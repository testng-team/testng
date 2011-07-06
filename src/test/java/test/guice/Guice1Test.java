package test.guice;

import com.google.inject.Inject;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Guice(modules = GuiceExampleModule.class)
public class Guice1Test extends SimpleBaseTest {
  static ISingleton m_object;

  @Inject
  ISingleton m_singleton;

  @Test
  public void singletonShouldWork() {
    m_singleton.doSomething();
    m_object = m_singleton;
  }

}
