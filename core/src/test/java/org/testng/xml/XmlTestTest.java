package org.testng.xml;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import test.SimpleBaseTest;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlTestTest extends SimpleBaseTest {
  @Test
  public void testNameMatchesAny() {
    XmlSuite xmlSuite = createDummySuiteWithTestNamesAs("test1");
    XmlTest xmlTest = xmlSuite.getTests().get(0);
    assertThat(xmlTest.nameMatchesAny(Collections.singletonList("test1"))).isTrue();
    assertThat(xmlTest.nameMatchesAny(Collections.singletonList("test2"))).isFalse();
  }

  @Test(dataProvider = "dp", description = "GITHUB-1716")
  public void testNullOrEmptyParameter(Map<String, String> data) {
    XmlTest test = createXmlTest("suite", "test", Issue1716TestSample.class);
    test.setParameters(data);
    test.toXml("   ");
    Assert.assertTrue(true, "No exceptions should have been thrown");
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{newSetOfParameters(null, "value")}, {newSetOfParameters("foo", null)}};
  }

  private static Map<String, String> newSetOfParameters(String key, String value) {
    Map<String, String> map = Maps.newHashMap();
    map.put(key, value);
    return map;
  }
}
