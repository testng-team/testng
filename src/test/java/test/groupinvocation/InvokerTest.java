package test.groupinvocation;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class InvokerTest extends SimpleBaseTest {
  private static final String SMOKE = "smoketests";
  private static final String FUNCTIONAL_TESTS = "functionaltests";

  @Test
  public void testClassWithRedundantGroups() {
    Assert.assertEquals(2, privateRun(RedundantGroupNamesSample.class, SMOKE, FUNCTIONAL_TESTS));
  }

  @Test
  public void testClassWithUniqueGroups() {
    Assert.assertEquals(2, privateRun(UniqueGroupNamesSample.class, SMOKE));
  }

  private int privateRun(final Class<?> className, String... groupNames) {
    XmlSuite suite = createXmlSuite("simple-suite");
    XmlTest xmlTest = createXmlTest(suite, "simple-test", className);
    for (String group : groupNames) {
      xmlTest.addIncludedGroup(group);
    }
    TestNG tng = create(suite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    return listener.getInvokedMethodNames().size();
  }

  public static class UniqueGroupNamesSample {
    @BeforeGroups(groups = {InvokerTest.SMOKE})
    public void before() {}

    @Test(groups = {InvokerTest.SMOKE})
    public void test() {}
  }

  public static class RedundantGroupNamesSample {

    @BeforeGroups(groups = {InvokerTest.SMOKE, InvokerTest.FUNCTIONAL_TESTS})
    public void before() {}

    @Test(groups = {InvokerTest.SMOKE, InvokerTest.FUNCTIONAL_TESTS})
    public void test() {}
  }
}
