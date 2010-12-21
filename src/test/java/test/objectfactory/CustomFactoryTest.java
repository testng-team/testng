package test.objectfactory;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import test.TestHelper;

/**
 * @author Hani Suleiman Date: Mar 6, 2007 Time: 3:52:19 PM
 */
public class CustomFactoryTest {
  @Test
  public void setFactoryOnTestNG() {
    XmlSuite suite = TestHelper.createSuite("test.objectfactory.Simple", "objectfactory");
    // suite.setObjectFactory(new LoggingObjectFactory());
    TestNG tng = TestHelper.createTestNG(suite);
    tng.setObjectFactory(LoggingObjectFactory.class);
    tng.run();
    assert LoggingObjectFactory.invoked == 1 : "Logging factory invoked "
        + LoggingObjectFactory.invoked + " times";
  }

  @AfterMethod
  public void resetCount() {
    LoggingObjectFactory.invoked = 0;
  }

  @Test
  public void setFactoryOnSuite() {
    XmlSuite suite = TestHelper.createSuite("test.objectfactory.Simple", "objectfactory");
    suite.setObjectFactory(new LoggingObjectFactory());
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();
    assert LoggingObjectFactory.invoked == 1 : "Logging factory invoked "
        + LoggingObjectFactory.invoked + " times";
  }

  @Test(enabled = false, description = "This broke after I made the change to enable AbstractTest")
  public void setFactoryByAnnotation() {
    XmlSuite suite = TestHelper.createSuite("test.objectfactory.Simple", "objectfactory");
    suite.getTests().get(0).getXmlClasses()
        .add(new XmlClass("test.objectfactory.MyFactoryFactory"));
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();
    assert LoggingObjectFactory.invoked == 1 : "Logging factory invoked "
        + LoggingObjectFactory.invoked + " times";
  }

  @Test
  public void factoryReceivesContext() {
    XmlSuite suite = TestHelper.createSuite("test.objectfactory.Simple", "objectfactory");
    suite.getTests().get(0).getXmlClasses()
        .add(new XmlClass("test.objectfactory.ContextAwareFactoryFactory"));
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();
  }

  @Test(expectedExceptions = TestNGException.class)
  public void setInvalidMethodFactoryByAnnotation() {
    XmlSuite suite = TestHelper.createSuite("test.objectfactory.Simple", "objectfactory");
    suite.getTests().get(0).getXmlClasses()
        .add(new XmlClass("test.objectfactory.BadMethodFactoryFactory"));
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();
  }
}
