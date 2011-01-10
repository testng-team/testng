package test.objectfactory;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.TestHelper;

/**
 * Test IObjectFactory2, which is an object factory that receives just the Class in parameter.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class ObjectFactory2Test {

  private void testFactory(boolean onSuite) {
    XmlSuite suite = TestHelper.createSuite(ClassObjectFactorySampleTest.class.getName(),
        "Test IObjectFactory2");
    TestNG tng = TestHelper.createTestNG(suite);

    if (onSuite) suite.setObjectFactory(new ClassObjectFactory());
    else tng.setObjectFactory(ClassObjectFactory.class);

    ClassObjectFactorySampleTest.m_n = 0;
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
