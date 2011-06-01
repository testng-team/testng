package test.xml;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class XmlVerifyTest extends SimpleBaseTest {

//  private String getFinalPath(String file) {
//    File currentDir = new File(".");
//    String path = currentDir.getAbsolutePath();
//    char s = File.separatorChar;
//    String testDir = System.getProperty("test.dir");
//    System.out.println("[XmlVerifyTest] test.dir:" + testDir);
//    Assert.assertNotNull(testDir);
//    path = path + s + testDir + s;
//    return path + file;
//  }

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

  @Test(description="Ensure that TestNG stops without running any tests if some class" +
      " included in suite is missing")
  public void handleInvalidSuites() {
     TestListenerAdapter tla = new TestListenerAdapter();
     try {
        TestNG tng = create();
        String testngXmlPath = getPathToResource("suite1.xml");
        tng.setTestSuites(Arrays.asList(testngXmlPath));
        tng.addListener(tla);
        tng.run();
     } catch (TestNGException ex) {
        Assert.assertEquals(tla.getPassedTests().size(), 0);
     }
  }
}
