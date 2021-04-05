package test.graph;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphVisualiserTest extends SimpleBaseTest {
  @Test(dataProvider = "dp")
  public void testVisualiserInvocation(Class<?> testClass, boolean injectListener) {
    TestNG testng = create();
    XmlSuite suite = createXmlSuite("test_suite");
    LocalVisualiser visualiser = null;
    if (injectListener) {
      visualiser = new LocalVisualiser();
      testng.addListener(visualiser);
    }
    createXmlTest(suite, "test", testClass);
    testng.setXmlSuites(Collections.singletonList(suite));
    testng.run();
    if (visualiser == null) {
      visualiser = LocalVisualiser.getInstance();
    }
    assertThat(visualiser.getDefinitions()).hasSize(2);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {TestSampleWithListener.class, true},
      {TestSampleWithoutListener.class, false}
    };
  }
}
