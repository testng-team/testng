package test.preserveorder;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class Issue1122Sample {

  @Test
  public void test(ITestContext context) {
    assertThat(context.getCurrentXmlTest().getPreserveOrder())
        .isEqualTo(XmlSuite.DEFAULT_PRESERVE_ORDER);
  }
}
