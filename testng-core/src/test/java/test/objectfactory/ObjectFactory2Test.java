package test.objectfactory;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/** Test IObjectFactory2, which is an object factory that receives just the Class in parameter. */
public class ObjectFactory2Test extends SimpleBaseTest {

  private void testFactory(boolean onSuite) {
    ClassObjectFactorySampleTest.m_n = 0;

    XmlSuite suite =
        createXmlSuite("Test IObjectFactory2", "TmpTest", ClassObjectFactorySampleTest.class);
    TestNG tng = create(suite);

    if (onSuite) {
      suite.setObjectFactoryClass(ClassObjectFactory.class);
    } else {
      tng.setObjectFactory(ClassObjectFactory.class);
    }

    tng.run();

    Assert.assertEquals(ClassObjectFactorySampleTest.m_n, 42);
  }

  @Test
  public void factoryOnSuiteShouldWork() {
    testFactory(true /* on suite object */);
  }

  @Test
  public void factoryOnTestNGShouldWork() {
    testFactory(false /* on TestNG object */);
  }
}
