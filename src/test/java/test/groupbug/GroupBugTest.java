package test.groupbug;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupBugTest extends SimpleBaseTest {

  @Test(description = "Comment out dependsOnGroups in ITCaseOne will fix the ordering, that's the bug")
  public void shouldOrderByClass() {
    TestNG tng = create(ITCaseOne.class, ITCaseTwo.class);
    tng.setVerbose(10);
    tng.setGroupByInstances(true);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames()).containsExactly(
            "beforeClassOne", "one1", "one2", "afterClassOne",
            "beforeClassTwo", "two1", "two2", "afterClassTwo"
    );
  }
}
