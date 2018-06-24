package test.factory.issue1745;

import org.testng.ITestContext;
import org.testng.annotations.Factory;
import org.testng.xml.XmlTest;

public class SuiteXmlPoweredFactoryTest {

  @Factory
  public Object[] createInstances(ITestContext ctx) {
    return new Object[] {new TestClassSample(ctx, "testcontext")};
  }

  @Factory
  public Object[] createInstances(XmlTest currentXmlTest) {
    return new Object[] {new TestClassSample(currentXmlTest, "xmltest")};
  }

  @Factory
  public Object[] createInstances(ITestContext ctx, XmlTest currentXmlTest) {
    return new Object[] {new TestClassSample(ctx, currentXmlTest)};
  }

  @Factory
  public Object[] createInstances(XmlTest currentXmlTest, ITestContext ctx) {
    return new Object[] {new TestClassSample(ctx, currentXmlTest)};
  }
}
