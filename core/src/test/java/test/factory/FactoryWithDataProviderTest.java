package test.factory;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.factory.issue1770.SampleTestFour;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FactoryWithDataProviderTest extends SimpleBaseTest {

  /** Verify that a factory can receive a data provider */
  @Test
  public void verifyDataProvider() {
    TestNG tng = create(FactoryWithDataProvider.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    assertThat(tla.getPassedTests()).hasSize(4);
  }

  private static final String RANDOM_VALUE = "random_value";

  @Test(description = "GITHUB-1770")
  public void verifyDataProvider2() {
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    XmlSuite xmlSuite = createXmlSuite("xml_suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "xml_test", SampleTestFour.class);
    Map<String, String> parameters = Maps.newHashMap();
    parameters.put("isCustom", RANDOM_VALUE);
    xmlTest.setParameters(parameters);
    TestNG testng = create(xmlSuite);
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getLogs("test"))
        .containsExactlyElementsOf(Collections.singletonList(RANDOM_VALUE));
  }
}
