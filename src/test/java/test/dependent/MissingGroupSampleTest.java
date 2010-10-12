package test.dependent;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.Parser;

import java.io.ByteArrayInputStream;

public class MissingGroupSampleTest {

  @Test(dependsOnGroups = {"missing-group"})
  public void shouldBeSkipped() {

  }

  @Test(dependsOnGroups = {"missing-group"}, ignoreMissingDependencies=true)
  public void shouldNotBeSkipped() {

  }

  public static void main(String[] args) throws Exception {
    TestNG tng = new TestNG();
    String xml = "<suite name=\"dgf\" verbose=\"10\"><test name=\"dgf\"><classes><class name=\"test.dependent.MissingGroupSampleTest\"></class></classes></test></suite>";
    System.out.println(xml);
    ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
    tng.setXmlSuites(new Parser(is).parseToList());
    tng.run();
  }
}
