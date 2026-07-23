package test.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class CustomFactoryTest extends SimpleBaseTest {

  @BeforeMethod
  public void resetCount() {
    LoggingObjectFactory.invoked = 0;
  }

  @Test
  public void setFactoryOnTestNG() {
    TestNG tng = create(SimpleSample.class);
    tng.setObjectFactory(LoggingObjectFactory.class);
    tng.run();

    assertThat(LoggingObjectFactory.invoked).isEqualTo(1);
  }

  @Test
  public void setFactoryOnSuite() {
    XmlSuite suite = createXmlSuite("objectfactory", "TmpTest", SimpleSample.class);
    suite.setObjectFactoryClass(LoggingObjectFactory.class);
    TestNG tng = create(suite);
    tng.run();

    assertThat(LoggingObjectFactory.invoked).isEqualTo(1);
  }

  @Test(description = "This broke after I made the change to enable AbstractTest")
  public void setFactoryByAnnotation() {
    XmlSuite suite =
        createXmlSuite(
            "objectfactory", "TmpTest", SimpleSample.class, MyObjectFactoryFactory.class);
    TestNG tng = create(suite);
    tng.run();

    assertThat(LoggingObjectFactory.invoked).isEqualTo(1);
  }

  @Test
  public void factoryReceivesContext() {
    XmlSuite suite =
        createXmlSuite(
            "objectfactory", "TmpTest", SimpleSample.class, ContextAwareObjectFactoryFactory.class);
    suite.setObjectFactoryClass(LoggingObjectFactory.class);
    TestNG tng = create(suite);
    tng.run();

    assertThat(ContextAwareObjectFactoryFactory.invoked).isEqualTo(1);
  }

  @Test(expectedExceptions = TestNGException.class)
  public void setInvalidMethodFactoryByAnnotation() {
    XmlSuite suite =
        createXmlSuite(
            "objectfactory", "TmpTest", SimpleSample.class, BadMethodObjectFactoryFactory.class);
    TestNG tng = create(suite);
    tng.run();
  }
}
