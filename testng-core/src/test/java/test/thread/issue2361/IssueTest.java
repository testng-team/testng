package test.thread.issue2361;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(dataProvider = "dp")
  public void ensureClassLevelSingleThreadedNatureGetsHonoured(Class<?> cls) {
    XmlSuite suite = createXmlSuite("Sample_Suite");
    suite.setParallel(ParallelMode.METHODS);
    XmlTest xmlTest = createXmlTest(suite, "Sample_Test", cls);
    xmlTest.setParallel(ParallelMode.METHODS);
    TestNG testng = create(suite);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  @DataProvider(name = "dp")
  public Object[][] getTestData() {
    return new Object[][] {{ChildClassExample.class}, {FactorySample.class}};
  }
}
