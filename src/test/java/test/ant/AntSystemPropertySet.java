package test.ant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

/**
 * Test whether nested propertysets are passed passed from the ant task. Executed by the "run:antprop" target
 * in test/build.xml.
 *
 * @author <a href="mailto:ttopwells@gmail.com">Todd Wells</a>
 */
public class AntSystemPropertySet {

  @Test
  public void outputTestProperties()
  {
    assertNotNull(System.getProperty("syspropset1"), "syspropset1 not found");
    assertEquals(System.getProperty("syspropset1"), "value 1", "Wrong value for syspropset1");

    assertNotNull(System.getProperty("syspropset2"), "syspropset2 not found");
    assertEquals(System.getProperty("syspropset2"), "value 2", "Wrong value for syspropset2");

    assertNotNull(System.getProperty("sysprop1"), "sysprop1 not found");
    assertEquals(System.getProperty("sysprop1"), "value 3", "Wrong value for sysprop1");
  }
}
