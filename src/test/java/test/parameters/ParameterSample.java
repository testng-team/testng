package test.parameters;

import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.Parser;

import java.io.ByteArrayInputStream;

/**
 * This class
 *
 * @author Cedric Beust, Jul 22, 2004
 *
 */

public class ParameterSample {

  @Parameters({ "first-name" })
  @BeforeMethod
  public void beforeTest(String firstName) {
//    System.out.println("[ParameterSample] Invoked beforeTestMethod with: " + firstName);
    assert "Cedric".equals(firstName)
     : "Expected Cedric, got " + firstName;
  }


  @Parameters({ "first-name" })
  @Test(groups = { "singleString"})
  public void testSingleString(String firstName) {
//    System.out.println("[ParameterSample] Invoked testString " + firstName);
    assert "Cedric".equals(firstName);
  }

  @Parameters({"this parameter doesn't exist"})
  @Test
  public void testNonExistentParameter(@Optional String foo) {

  }

  public static void main(String[] args) throws Exception {
    TestNG tng = new TestNG();
    String xml = "<suite name=\"dgf\" verbose=\"10\"><parameter name=\"first-name\" value=\"Cedric\" /><test name=\"dgf\"><classes><class name=\"test.parameters.ParameterSample\"></class></classes></test></suite>";
    System.out.println(xml);
    ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
    tng.setXmlSuites(new Parser(is).parseToList());
    tng.run();
  }

}
