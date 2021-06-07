package test.preserveorder;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class Issue1122Sample {

  @Test
  public void test(ITestContext context) {
    Assert.assertEquals(
        context.getCurrentXmlTest().getPreserveOrder(), XmlSuite.DEFAULT_PRESERVE_ORDER);
  }
}
