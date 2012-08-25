package test.guice;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import junit.framework.Assert;

public class GuiceTest extends SimpleBaseTest {

  @Test
  public void guiceTest() {
    TestNG tng = create(new Class[] { Guice1Test.class, Guice2Test.class});
    Guice1Test.m_object = null;
    Guice2Test.m_object = null;
    tng.run();

    Assert.assertNotNull(Guice1Test.m_object);
    Assert.assertNotNull(Guice2Test.m_object);
    Assert.assertEquals(Guice1Test.m_object, Guice2Test.m_object);
  }

  @Test
  public void guiceWithNoModules() {
    TestNG tng = create(new Class[] { GuiceNoModuleTest.class });
    tng.run();
  }
}
