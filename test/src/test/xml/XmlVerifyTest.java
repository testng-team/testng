package test.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

public class XmlVerifyTest extends SimpleBaseTest {
   
  private String getFinalPath(String file) {
    File currentDir = new File(".");
    String path = currentDir.getAbsolutePath();
    char s = File.separatorChar;
    path = path + s + "test" + s + "src" + s + "test" + s + "xml" + s;
    return path + file;
  }

  @Test
  public void simple() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    XmlClass xClass = new XmlClass(XmlVerifyTest.class);
    test.getXmlClasses().add(xClass);
    test.getExcludedGroups().add("fast");
    test.setVerbose(5);
    
    suite.toXml();
  }
  
  @Test(description="Checks to make sure that NPE is not thrown when <test> tag is missing")
  public void handleInvalidXml() {
     OutputStream os = new ByteArrayOutputStream();
     PrintStream out = System.out;
     PrintStream err = System.err;
     try {
        /*
         * Changing system print streams so that exception is not logged 
         * while running test (avoid confusing person running tests)
         */
        System.setOut(new PrintStream(os));
        System.setErr(new PrintStream(os));
        TestNG tng = create();
        String testngXmlPath = getFinalPath("invalid-testng.xml");
        tng.setTestSuites(Arrays.asList(testngXmlPath));
        tng.run();
     } finally {
        try {
         os.close();
      } catch (IOException e) {
         //no need to handle this
      }
        System.setOut(out);
        System.setErr(err);
     }
  }
}
