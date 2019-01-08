package test.parameters.issue1994;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

@Listeners(TestclassSample.class)
public class TestclassSample implements ISuiteListener {

  public static int count = 0;

  @BeforeClass
  public void beforeClass(XmlTest xmlTest) {
  }

  @Test
  public void testMethod() {
  }

  @Override
  public void onFinish(ISuite suite) {
    setCount(suite.getXmlSuite().getTests().size());
  }

  private static void setCount(int count) {
    TestclassSample.count = count;
  }

}
