package test.preserveorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class PreserveOrderTest extends SimpleBaseTest {

  @DataProvider
  public static Object[][] dpTests() {
    return new Class<?>[][] {
      new Class<?>[] {A.class, B.class, C.class},
      new Class<?>[] {A.class, C.class, B.class},
      new Class<?>[] {B.class, A.class, C.class},
      new Class<?>[] {B.class, C.class, A.class},
      new Class<?>[] {C.class, B.class, A.class},
      new Class<?>[] {C.class, A.class, B.class}
    };
  }

  @Test(dataProvider = "dpTests")
  public void preserveClassOrder(Class<?>[] tests) {
    TestNG tng = create();
    XmlSuite suite = createXmlSuite("Suite");
    XmlTest test = createXmlTest(suite, "Test", tests);
    test.setPreserveOrder(true);
    tng.setXmlSuites(Arrays.asList(suite));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames()).hasSize(9);
    Iterator<String> methods = listener.getInvokedMethodNames().iterator();
    for (Class<?> testClass : tests) {
      for (int i = 1; i <= 3; i++) {
        String methodName = methods.next();
        Assert.assertEquals(methodName, testClass.getSimpleName().toLowerCase() + i);
      }
    }
  }

  @DataProvider
  public static Object[][] dpMethods() {
    return new String[][] {
      new String[] {"a1", "a2", "a3"},
      new String[] {"a1", "a3", "a2"},
      new String[] {"a2", "a1", "a3"},
      new String[] {"a2", "a3", "a1"},
      new String[] {"a3", "a2", "a1"},
      new String[] {"a3", "a1", "a2"}
    };
  }

  @Test(dataProvider = "dpMethods")
  public void preserveMethodOrder(String[] methods) {
    TestNG tng = create();
    XmlSuite suite = createXmlSuite("Suite");
    XmlTest test = createXmlTest(suite, "Test", A.class);
    addMethods(test.getXmlClasses().get(0), methods);
    test.setPreserveOrder(true);
    tng.setXmlSuites(Arrays.asList(suite));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames()).containsExactly(methods);
  }

  @Test
  public void orderShouldBePreservedWithDependencies() {
    TestNG tng = create();
    XmlSuite suite = createXmlSuite("PreserveOrder");
    XmlTest test = createXmlTest(suite, "Test", Chuck4Sample.class, Chuck3Sample.class);
    test.setPreserveOrder(true);
    tng.setXmlSuites(Arrays.asList(suite));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames())
        .containsExactly(
            "c4TestOne", "c4TestTwo", "c4TestThree", "c3TestOne", "c3TestTwo", "c3TestThree");
  }

  @Test(description = "preserve-order on a factory doesn't cause an NPE")
  public void factoryPreserve() {
    TestNG tng = create();
    XmlSuite suite = createXmlSuite("FactoryPreserve");
    XmlTest test = createXmlTest(suite, "Test", ClassSampleFactory.class);
    test.setPreserveOrder(true);
    tng.setXmlSuites(Arrays.asList(suite));

    tng.run();
  }

  @Test(description = "GITHUB-1122 Use default value for preserve-order")
  public void preserveOrderValueShouldBeTheDefaultOne() {
    TestNG tng = create(Issue1122Sample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
  }
}
