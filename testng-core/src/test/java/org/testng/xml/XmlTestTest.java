package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.junitreports.SimpleTestSample;

public class XmlTestTest extends SimpleBaseTest {
  @Test
  public void testNameMatchesAny() {
    XmlSuite xmlSuite = createDummySuiteWithTestNamesAs("test1");
    XmlTest xmlTest = xmlSuite.getTests().get(0);
    assertThat(xmlTest.nameMatchesAny(Collections.singletonList("test1"))).isTrue();
    assertThat(xmlTest.nameMatchesAny(Collections.singletonList("test2"))).isFalse();
  }

  @Test(description = "GITHUB-3196")
  public void testNameMatchesAnyWithRegex() {
    XmlSuite xmlSuite = createDummySuiteWithTestNamesAs("test1");
    XmlTest xmlTest = xmlSuite.getTests().get(0);
    assertThat(xmlTest.nameMatchesAny(Collections.singletonList("/^(test1$).*/"))).isTrue();
    assertThat(xmlTest.nameMatchesAny(Collections.singletonList("/^(?!test1$).*/"))).isFalse();
  }

  @Test(dataProvider = "dp", description = "GITHUB-1716")
  public void testNullOrEmptyParameter(Map<String, String> data) {
    XmlTest test = createXmlTest("suite", "test", Issue1716TestSample.class);
    test.setParameters(data);
    test.toXml("   ");
    assertThat(true).withFailMessage("No exceptions should have been thrown").isTrue();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{newSetOfParameters(null, "value")}, {newSetOfParameters("foo", null)}};
  }

  @Test(description = "GITHUB-2467")
  public void testXMLClassesInCloneMethod() {
    XmlSuite xmlSuite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "test");
    createXmlClass(xmlTest, SimpleTestSample.class);
    XmlTest copyXmlTest = (XmlTest) xmlTest.clone();
    assertThat(copyXmlTest).isNotNull();
    assertThat(copyXmlTest.getXmlClasses()).isNotNull();
    assertThat(xmlTest.getXmlClasses().size()).isEqualTo(copyXmlTest.getXmlClasses().size());
  }

  @Test(description = "GITHUB-3385")
  public void addIncludedGroupRepairsGroupsWithoutRun() {
    XmlTest test = createXmlTest(createXmlSuite("suite"), "test");
    test.setGroups(new XmlGroups());
    test.addIncludedGroup("g1");
    assertThat(test.getIncludedGroups()).containsExactly("g1");
  }

  @Test(description = "GITHUB-3385")
  public void addIncludedGroupAfterAddMetaGroupRepairsMissingRun() {
    XmlTest test = createXmlTest(createXmlSuite("suite"), "test");
    test.addMetaGroup("mg", "g1");
    test.addIncludedGroup("g2");
    assertThat(test.getIncludedGroups()).containsExactly("g2");
    assertThat(test.getMetaGroups()).containsKey("mg");
  }

  @Test(description = "GITHUB-3385")
  public void equalsWhenBothGroupsHaveNoRun() {
    XmlTest left = createXmlTest(createXmlSuite("suite1"), "test");
    left.setGroups(new XmlGroups());
    XmlTest right = createXmlTest(createXmlSuite("suite2"), "test");
    right.setGroups(new XmlGroups());
    assertThat(left).isEqualTo(right);
    assertThat(right).isEqualTo(left);
  }

  @Test(description = "GITHUB-3385")
  public void equalsWhenOnlyOneSideHasARun() {
    XmlTest withRun = createXmlTest(createXmlSuite("suite1"), "test");
    XmlGroups groups = new XmlGroups();
    groups.setRun(new XmlRun());
    withRun.setGroups(groups);
    XmlTest withoutRun = createXmlTest(createXmlSuite("suite2"), "test");
    withoutRun.setGroups(new XmlGroups());
    assertThat(withRun).isNotEqualTo(withoutRun);
    assertThat(withoutRun).isNotEqualTo(withRun);
  }

  /**
   * GITHUB-1716 needs a map with a null key or value, but {@link XmlTest#setParameters(Map)} takes
   * a {@code Map<String, String>}, so the map cannot be typed nullably and still be passed.
   */
  @SuppressWarnings("NullAway")
  private static Map<String, String> newSetOfParameters(
      @Nullable String key, @Nullable String value) {
    Map<String, String> map = new HashMap<>();
    map.put(key, value);
    return map;
  }
}
