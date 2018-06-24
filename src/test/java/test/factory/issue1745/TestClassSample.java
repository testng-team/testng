package test.factory.issue1745;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

public class TestClassSample {
  private ITestContext ctx;
  private XmlTest currentXmlTest;
  private String whatWasPassed = "";

  public TestClassSample(ITestContext ctx, String whatWasPassed) {
    this.ctx = ctx;
    this.whatWasPassed = whatWasPassed;
  }

  public TestClassSample(XmlTest currentXmlTest, String whatWasPassed) {
    this.currentXmlTest = currentXmlTest;
    this.whatWasPassed = whatWasPassed;
  }

  public TestClassSample(ITestContext ctx, XmlTest currentXmlTest) {
    this.ctx = ctx;
    this.currentXmlTest = currentXmlTest;
  }

  @Test
  public void testMethod() {
    if ("testcontext".equalsIgnoreCase(whatWasPassed)) { // only testcontext was provided
      assertNotNull(ctx);
      currentXmlTest = ctx.getCurrentXmlTest();
    }
    assertNotNull(currentXmlTest);
    int number = Integer.parseInt(currentXmlTest.getParameter("number"));
    assertTrue(number != 0);
  }
}
