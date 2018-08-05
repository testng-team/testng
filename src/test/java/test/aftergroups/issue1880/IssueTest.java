package test.aftergroups.issue1880;


import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.FailurePolicy;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1880")
  public void ensureAfterGroupsAreInvokedWithAlwaysRunAttribute() {

    XmlSuite xmlsuite = createXmlSuite("sample_suite", "sample_test", TestClassSample.class);
    xmlsuite.addIncludedGroup("123");
    TestNG testng = create(xmlsuite);
    testng.setConfigFailurePolicy(FailurePolicy.CONTINUE);
    LocalConfigListener listener = new LocalConfigListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getMessages()).containsExactly("after");
  }
}
