package test.reflect;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;
import org.testng.internal.reflect.DataProviderMethodMatcher;
import org.testng.internal.reflect.MethodMatcher;
import org.testng.internal.reflect.MethodMatcherContext;
import org.testng.internal.reflect.MethodMatcherException;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlTest;

/**
 * Created on 12/24/15
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class TestMethodMatcher {
  private static final Logger log = Logger.getLogger(TestMethodMatcher.class);

  private static Method getMethod(final String methodName) {
    Method method = null;
    for (final Method m : TestMethodMatcher.class.getMethods()) {
      if (m.getName().equals(methodName)) {
        method = m;
      }
    }
    return method;
  }

  @DataProvider
  public Object[][] methodParamPairs() {
    return new Object[][] {
      new Object[] {"goodTestIssue122", new Object[] {"3", new String[] {"three", "four"}}},
      new Object[] {"badTestIssue122", new Object[] {"3", new String[] {"three", "four"}}},
      new Object[] {"goodTestIssue122", new Object[] {"3", "three", "four"}},
      new Object[] {"badTestIssue122", new Object[] {"3", "three", "four"}},
      new Object[] {"mixedArgs", new Object[] {3, true, new String[] {"three"}, "four"}},
      new Object[] {
        "mixedArgs", new Object[] {3, true, new String[] {"three"}, new String[] {"four"}}
      },
      new Object[] {
        "potpourri0",
        new Object[] {
          getMethod("mixedArgs"),
          new XmlTestJustForTesting(),
          3,
          getMethod("badTestIssue122"),
          new TestContextJustForTesting(),
          true,
          new TestResultJustForTesting(),
          new String[] {"three"},
          new String[] {"four"}
        }
      },
      new Object[] {
        "potpourri1",
        new Object[] {
          getMethod("mixedArgs"),
          new XmlTestJustForTesting(),
          3,
          getMethod("badTestIssue122"),
          new TestContextJustForTesting(),
          true,
          new TestResultJustForTesting(),
          new String[] {"three"},
          new String[] {"four"}
        }
      },
    };
  }

  @DataProvider
  public Object[][] methodParamFailingPairs() {
    return new Object[][] {
      new Object[] {"goodTestIssue122", new Object[] {3, "three", "four"}},
      new Object[] {"badTestIssue122", new Object[] {3, "three", "four"}},
      new Object[] {"mixedArgs", new Object[] {3, true, "three", "four"}},
    };
  }

  @Test(dataProvider = "methodParamPairs")
  public void testMatcher(
      final String methodName,
      final Object[] params,
      final ITestContext iTestContext,
      final ITestResult iTestResult)
      throws Throwable {
    final Method method = getMethod(methodName);
    final MethodMatcher matcher =
        new DataProviderMethodMatcher(
            new MethodMatcherContext(method, params, iTestContext, iTestResult));
    method.invoke(new TestMethodMatcher(), matcher.getConformingArguments());
  }

  @Test(dataProvider = "methodParamFailingPairs")
  public void testNegativeCaseMatcher(
      final String methodName,
      final Object[] params,
      final ITestContext iTestContext,
      final ITestResult iTestResult) {
    final Method method = getMethod(methodName);
    final MethodMatcher matcher =
        new DataProviderMethodMatcher(
            new MethodMatcherContext(method, params, iTestContext, iTestResult));
    Assert.assertFalse(matcher.conforms());
    assertThatThrownBy(
            () -> {
              method.invoke(new TestMethodMatcher(), matcher.getConformingArguments());
            })
        .isInstanceOf(MethodMatcherException.class)
        // separate lines are used here to avoid \n vs \r\n if running tests in Windows
        .hasMessageContaining(
            "has no parameters defined but was found to be using a data provider (either explicitly specified or inherited from class level annotation")
        .hasMessageContaining("Method: ")
        .hasMessageContaining("Arguments: ");
  }

  public void goodTestIssue122(String s, String[] strings) {
    for (String item : strings) {
      log.debug("An item is \"" + item + "\"");
    }
    Assert.assertEquals(s, "3");
  }

  public void badTestIssue122(String s, String... strings) {
    for (String item : strings) {
      log.debug("An item is \"" + item + "\"");
    }
    Assert.assertEquals(s, "3");
  }

  public void mixedArgs(final int i, final Boolean b, final String[] s1, final String... strings) {
    for (String item : strings) {
      log.debug("An item is \"" + item + "\"");
    }
    Assert.assertEquals(i, 3);
    Assert.assertNotNull(b);
    Assert.assertTrue(b);
    Assert.assertNotNull(s1);
    Assert.assertEquals(s1.length, 1);
    Assert.assertEquals(s1[0], "three");
    Assert.assertNotNull(strings);
    Assert.assertEquals(strings.length, 1);
    Assert.assertEquals(strings[0], "four");
  }

  public void potpourri0(
      @NoInjection final Method myMethod1,
      @NoInjection final XmlTest myXmlTest,
      final Method currentTestMethod,
      final int i,
      final Method myMethod2,
      final ITestContext iTestContext,
      @NoInjection final ITestContext myTestContext,
      final Boolean b,
      @NoInjection final ITestResult myTestResult,
      final ITestResult iTestResult,
      final String[] s1,
      final XmlTest xmlTest,
      final String... strings) {
    log.debug("MyMethod1 is \"" + myMethod1 + "\"");
    log.debug("MyMethod2 is \"" + myMethod2 + "\"");
    log.debug("CurrentTestMethod is \"" + currentTestMethod + "\"");
    log.debug("MyITestContext is \"" + myTestContext + "\"");
    log.debug("ITestContext is \"" + iTestContext + "\"");
    log.debug("ITestResult is \"" + iTestResult + "\"");
    log.debug("MyTestResult is \"" + myTestResult + "\"");
    log.debug("XmlTest is \"" + xmlTest + "\"");
    log.debug("MyXmlTest is \"" + myXmlTest + "\"");
    for (String item : strings) {
      log.debug("An item is \"" + item + "\"");
    }
    Assert.assertNotNull(myTestContext);
    Assert.assertTrue(myTestContext instanceof TestContextJustForTesting);
    Assert.assertNotNull(myTestResult);
    Assert.assertTrue(myTestResult instanceof TestResultJustForTesting);
    Assert.assertNotNull(myXmlTest);
    Assert.assertTrue(myXmlTest instanceof XmlTestJustForTesting);
    Assert.assertNotNull(currentTestMethod);
    Assert.assertEquals("potpourri0", currentTestMethod.getName());
    Assert.assertNotNull(myMethod1);
    Assert.assertEquals("mixedArgs", myMethod1.getName());
    Assert.assertNotNull(myMethod2);
    Assert.assertEquals("badTestIssue122", myMethod2.getName());
    Assert.assertEquals(i, 3);
    Assert.assertNotNull(b);
    Assert.assertTrue(b);
    Assert.assertNotNull(s1);
    Assert.assertEquals(s1.length, 1);
    Assert.assertEquals(s1[0], "three");
    Assert.assertNotNull(strings);
    Assert.assertEquals(strings.length, 1);
    Assert.assertEquals(strings[0], "four");
  }

  public void potpourri1(
      @NoInjection final Method myMethod1,
      @NoInjection final XmlTest myXmlTest,
      final Method currentTestMethod,
      final int i,
      final Method myMethod2,
      final ITestContext iTestContext,
      @NoInjection final ITestContext myTestContext,
      final Boolean b,
      @NoInjection final ITestResult myTestResult,
      final ITestResult iTestResult,
      final String[] s1,
      final XmlTest xmlTest,
      final String[] strings) {
    log.debug("MyMethod1 is \"" + myMethod1 + "\"");
    log.debug("MyMethod2 is \"" + myMethod2 + "\"");
    log.debug("CurrentTestMethod is \"" + currentTestMethod + "\"");
    log.debug("MyITestContext is \"" + myTestContext + "\"");
    log.debug("ITestContext is \"" + iTestContext + "\"");
    log.debug("ITestResult is \"" + iTestResult + "\"");
    log.debug("MyTestResult is \"" + myTestResult + "\"");
    log.debug("XmlTest is \"" + xmlTest + "\"");
    log.debug("MyXmlTest is \"" + myXmlTest + "\"");
    for (String item : strings) {
      log.debug("An item is \"" + item + "\"");
    }
    Assert.assertNotNull(myTestContext);
    Assert.assertTrue(myTestContext instanceof TestContextJustForTesting);
    Assert.assertNotNull(myTestResult);
    Assert.assertTrue(myTestResult instanceof TestResultJustForTesting);
    Assert.assertNotNull(myXmlTest);
    Assert.assertTrue(myXmlTest instanceof XmlTestJustForTesting);
    Assert.assertNotNull(currentTestMethod);
    Assert.assertEquals("potpourri1", currentTestMethod.getName());
    Assert.assertNotNull(myMethod1);
    Assert.assertEquals("mixedArgs", myMethod1.getName());
    Assert.assertNotNull(myMethod2);
    Assert.assertEquals("badTestIssue122", myMethod2.getName());
    Assert.assertEquals(i, 3);
    Assert.assertNotNull(b);
    Assert.assertTrue(b);
    Assert.assertNotNull(s1);
    Assert.assertEquals(s1.length, 1);
    Assert.assertEquals(s1[0], "three");
    Assert.assertNotNull(strings);
    Assert.assertEquals(strings.length, 1);
    Assert.assertEquals(strings[0], "four");
  }
}
