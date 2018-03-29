package test.guice;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class GuiceTest extends SimpleBaseTest {

  @Test
  public void guiceTest() {
    TestNG tng = create(Guice1Test.class, Guice2Test.class);
    Guice1Test.m_object = null;
    Guice2Test.m_object = null;
    tng.run();

    assertThat(Guice1Test.m_object).isNotNull();
    assertThat(Guice2Test.m_object).isNotNull();
    assertThat(Guice1Test.m_object).isEqualTo(Guice2Test.m_object);
  }

  @Test
  public void guiceWithNoModules() {
    TestNG tng = create(GuiceNoModuleTest.class);
    tng.run();
  }
}
