package test.guice;

import com.google.inject.Inject;

import org.testng.annotations.Test;

public class GuiceInheritanceTest extends GuiceBase {

  @Inject
  ISingleton m_singleton;

  @Test
  public void singletonShouldWork() {
    m_singleton.doSomething();
  }
}
