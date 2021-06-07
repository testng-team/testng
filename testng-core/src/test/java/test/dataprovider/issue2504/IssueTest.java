package test.dataprovider.issue2504;

import java.util.Arrays;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2504")
  public void ensureParametersCopiedOnConfigFailures() {
    XmlTest xmltest = createXmlTest("2504_suite", "2504_test");
    xmltest.setXmlClasses(Collections.singletonList(new XmlClass(SampleTestCase.class)));
    TestNG testNG = create(Collections.singletonList(xmltest.getSuite()));
    SampleTestCaseListener listener = new SampleTestCaseListener();
    testNG.addListener(listener);
    testNG.run();
    Assertions.assertThat(listener.getParameters())
        .containsExactlyElementsOf(Arrays.asList(1, 2, 3, 4, 5));
  }
}
