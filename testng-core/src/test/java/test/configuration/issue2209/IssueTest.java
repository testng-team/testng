package test.configuration.issue2209;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void testGithub2209() {
    TestNG tng = create(Sample.class);
    tng.setGroups("groupM");

    InvokedMethodNameListener listener = new InvokedMethodNameListener(false);
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeClass",
            "beforeMethod",
            "test1",
            "afterMethod",
            "beforeMethod",
            "test2",
            "afterMethod",
            "afterClass");
  }

  @Test(groups = {"groupM"})
  public static class Sample {

    @BeforeClass
    public void beforeClass() {}

    @BeforeMethod
    public void beforeMethod() {}

    @Test(groups = {"groupK"})
    public void test1() {}

    @Test(groups = {"groupL"})
    public void test2() {}

    @AfterMethod
    public void afterMethod() {}

    @AfterClass
    public void afterClass() {}
  }
}
