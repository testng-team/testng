package test.bug90;

import java.util.Collections;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class Bug90Test extends SimpleBaseTest {

  @Test(description = "Fix for https://github.com/cbeust/testng/issues/90")
  public void afterClassShouldRun() {
    XmlSuite s = createXmlSuite("Bug90");
    XmlTest t = createXmlTest(s, "Bug90 test", Sample.class.getName());
    XmlClass c = t.getClasses().get(0);
    c.setIncludedMethods(Collections.singletonList(new XmlInclude("test1")));
    TestNG tng = create();
    tng.setXmlSuites(Collections.singletonList(s));
    Sample.m_afterClassWasRun = false;
    tng.run();

    Assert.assertTrue(Sample.m_afterClassWasRun);
  }
}
