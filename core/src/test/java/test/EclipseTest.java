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
    String fileName = ctx.getSuite().getXmlSuite().getFileName().replace("\\", "/");
    Assert.assertTrue(fileName.contains("src/test/resources"));
  }
}
