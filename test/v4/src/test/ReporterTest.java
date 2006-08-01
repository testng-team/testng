package test;

import java.io.File;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class ReporterTest {

  private String getOutputDir() {
    return System.getProperty("java.io.tmpdir");
  }

  protected String getSuiteName() {
    return "TmpSuite";
  
  }
  @Test
  public void verifyIndex() {
    XmlSuite suite = TestHelper.createSuite("test.simple.SimpleTest", getSuiteName());

    File f = new File(getOutputDir() + File.separatorChar + getSuiteName()
        + File.separatorChar + "index.html");
    f.delete();
    Assert.assertFalse(f.exists());
    
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();
    Assert.assertTrue(f.exists());
    
    f.deleteOnExit();
  }

}
