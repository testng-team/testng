package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Checks to see if the parameters from parent suite are passed onto the
 * child suite (referred by <suite-file>)
 * @author nullin
 *
 */
public class InheritFromSuiteChild3
{
   @Test
   @Parameters({"parameter1", "parameter2", "parameter3", "parameter4"})
   public void inheritedparameter(String p1, String p2, String p3, String p4) {
      Assert.assertEquals(p1, "p1");
      Assert.assertEquals(p2, "c3p2");
      Assert.assertEquals(p3, "c2p3");
      Assert.assertEquals(p4, "c3p4");
   }
}
