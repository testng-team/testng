package test.reflect;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;
import org.testng.internal.reflect.DataProviderMethodMatcher;
import org.testng.internal.reflect.MethodMatcher;
import org.testng.internal.reflect.MethodMatcherContext;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;

/**
 * Created on 12/24/15
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class TestMethodMatcher {
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
    return new Object[][]{
      new Object[]{"goodTestIssue122", new Object[]{"3", new String[]{"three", "four"}}},
      new Object[]{"badTestIssue122", new Object[]{"3", new String[]{"three", "four"}}},
      new Object[]{"goodTestIssue122", new Object[]{"3", "three", "four"}},
      new Object[]{"badTestIssue122", new Object[]{"3", "three", "four"}},
      new Object[]{"mixedArgs", new Object[]{3, true, new String[]{"three"}, "four"}},
      new Object[]{"mixedArgs", new Object[]{3, true, new String[]{"three"}, new String[]{"four"}}},
      new Object[]{"potpourri0",
        new Object[]{
          getMethod("mixedArgs"),
          new XmlTestJustForTesting(),
          3,
          getMethod("badTestIssue122"),
          new TestContextJustForTesting(),
          true,
          new TestResultJustForTesting(),
          new String[]{"three"},
          new String[]{"four"}}
      },
      new Object[]{"potpourri1",
        new Object[]{
          getMethod("mixedArgs"),
          new XmlTestJustForTesting(),
          3,
          getMethod("badTestIssue122"),
          new TestContextJustForTesting(),
          true,
          new TestResultJustForTesting(),
          new String[]{"three"},
          new String[]{"four"}}
      },
    };
  }

  @DataProvider
  public Object[][] methodParamFailingPairs() {
    return new Object[][]{
      new Object[]{"goodTestIssue122", new Object[]{3, "three", "four"}},
      new Object[]{"badTestIssue122", new Object[]{3, "three", "four"}},
      new Object[]{"mixedArgs", new Object[]{3, true, "three", "four"}},
    };
  }


  @Test(dataProvider = "methodParamPairs")
  public void testMatcher(final String methodName, final Object[] params,
                          final ITestContext iTestContext, final ITestResult iTestResult) {
    final Method method = getMethod(methodName);
    final MethodMatcher matcher = new DataProviderMethodMatcher(
      new MethodMatcherContext(method, params, iTestContext, iTestResult));
    try {
      method.invoke(new TestMethodMatcher(), matcher.getConformingArguments());
    } catch (final Throwable throwable) {
      System.out.println("methodParamPairs failure");
      throwable.printStackTrace();
      Assert.fail("methodParamPairs failure");
    }
  }

  @Test(dataProvider = "methodParamFailingPairs")
  public void testNegativeCaseMatcher(final String methodName, final Object[] params,
                                      final ITestContext iTestContext, final ITestResult iTestResult) {
    final Method method = getMethod(methodName);
    final MethodMatcher matcher = new DataProviderMethodMatcher(
      new MethodMatcherContext(method, params, iTestContext, iTestResult));
    Assert.assertFalse(matcher.conforms());
    try {
      method.invoke(new TestMethodMatcher(), matcher.getConformingArguments());
      Assert.fail();
    } catch (final Throwable throwable) {
      throwable.printStackTrace();
      //noop
    }
  }

  public void goodTestIssue122(String s, String[] strings) {
    for (String item : strings) {
      System.out.println("An item is \"" + item + "\"");
    }
    Assert.assertEquals(s, "3");
  }

  public void badTestIssue122(String s, String... strings) {
    for (String item : strings) {
      System.out.println("An item is \"" + item + "\"");
    }
    Assert.assertEquals(s, "3");
  }

  public void mixedArgs(
    final int i, final Boolean b,
    final String[] s1, final String... strings
  ) {
    for (String item : strings) {
      System.out.println("An item is \"" + item + "\"");
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
    final String... strings
  ) {
    System.out.println("MyMethod1 is \"" + myMethod1 + "\"");
    System.out.println("MyMethod2 is \"" + myMethod2 + "\"");
    System.out.println("CurrentTestMethod is \"" + currentTestMethod + "\"");
    System.out.println("MyITestContext is \"" + myTestContext + "\"");
    System.out.println("ITestContext is \"" + iTestContext + "\"");
    System.out.println("ITestResult is \"" + iTestResult + "\"");
    System.out.println("MyTestResult is \"" + myTestResult + "\"");
    System.out.println("XmlTest is \"" + xmlTest + "\"");
    System.out.println("MyXmlTest is \"" + myXmlTest + "\"");
    for (String item : strings) {
      System.out.println("An item is \"" + item + "\"");
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
    final String[] strings
  ) {
    System.out.println("MyMethod1 is \"" + myMethod1 + "\"");
    System.out.println("MyMethod2 is \"" + myMethod2 + "\"");
    System.out.println("CurrentTestMethod is \"" + currentTestMethod + "\"");
    System.out.println("MyITestContext is \"" + myTestContext + "\"");
    System.out.println("ITestContext is \"" + iTestContext + "\"");
    System.out.println("ITestResult is \"" + iTestResult + "\"");
    System.out.println("MyTestResult is \"" + myTestResult + "\"");
    System.out.println("XmlTest is \"" + xmlTest + "\"");
    System.out.println("MyXmlTest is \"" + myXmlTest + "\"");
    for (String item : strings) {
      System.out.println("An item is \"" + item + "\"");
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
