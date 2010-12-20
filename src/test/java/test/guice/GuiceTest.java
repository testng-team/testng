package test.guice;

import com.google.inject.Inject;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Guice(modules = GuiceExampleModule.class)
public class GuiceTest extends SimpleBaseTest {

  @Inject
  ISingleton m_singleton;

  @Test
  public void singletonShouldWork() {
    m_singleton.doSomething();
  }

}
