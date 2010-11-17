package test;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * Make sure that these test pass when run by the Eclipse plug-in.
 *
 * @author Cedric Beust <cedric@beust.com>
 *
 */
public class EclipseTest {

  @Test
  public void xmlFileShouldBeRunAtItsPlaceAndNotCopied(ITestContext ctx) {
    Assert.assertTrue(ctx.getSuite().getXmlSuite().getFileName().contains("src/test/resources"));
  }
}
