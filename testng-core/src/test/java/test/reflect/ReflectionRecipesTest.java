package test.reflect;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestRunner;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;
import org.testng.internal.reflect.InjectableParameter;
import org.testng.internal.reflect.Parameter;
import org.testng.internal.reflect.ReflectionRecipes;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlTest;

/** @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a> */
public class ReflectionRecipesTest {
  private static final Logger log = Logger.getLogger(ReflectionRecipesTest.class);

  private static final Object[] A0 = new Object[] {343, true};
  private static final Parameter[] S0 = getMethodParameters(T.class, "s0");
  private static final Parameter[] S1 = getMethodParameters(T.class, "s1");
  private static final Parameter[] S2 = getMethodParameters(T.class, "s2");
  private static final Parameter[] S3 = getMethodParameters(T.class, "s3");

  private static Parameter[] getMethodParameters(final Class<?> clazz, final String methodName) {
    Method method = null;
    Parameter[] parameters = null;
    for (final Method m : clazz.getMethods()) {
      if (m.getName().equals(methodName)) {
        method = m;
      }
    }
    if (method != null) {
      parameters = ReflectionRecipes.getMethodParameters(method);
    }
    return parameters;
  }

  @DataProvider
  public Object[][] methodInputParameters() {
    return new Object[][] {S0, S1, S2, S3};
  }

  @DataProvider
  public Object[][] methodInputParamArgsPair() {
    return new Object[][] {
      new Object[] {S0, A0},
      new Object[] {S1, A0},
      new Object[] {S2, A0},
      new Object[] {S3, A0},
    };
  }

  @DataProvider
  public Object[][] exactMatchDP()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final Method[] methods = ExactMatchTest.class.getDeclaredMethods();
    final List<Object[]> objects = new ArrayList<>();
    log.debug("exactMatchDP:");
    for (int i = 0; i < methods.length; i++) {
      final Method method = methods[i];
      final ExactMatchTest.Expectation annotation =
          method.getAnnotation(ExactMatchTest.Expectation.class);
      if (annotation != null) {
        final String provider = annotation.expectationProvider();
        final int flag = annotation.flag();
        Assert.assertNotNull(provider);
        final Method providerMethod = ExactMatchTest.class.getMethod(provider, int.class);
        final Object out = providerMethod.invoke(ExactMatchTest.class, flag);
        Assert.assertTrue(out instanceof Object[][]);
        log.debug(method.getName() + ", " + out);
        objects.add(new Object[] {out, method});
      }
    }
    return objects.toArray(new Object[objects.size()][]);
  }

  @DataProvider
  public Object[][] matchArrayEndingDP()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final Method[] methods = MatchArrayEndingTest.class.getDeclaredMethods();
    final List<Object[]> objects = new ArrayList<>();
    log.debug("matchArrayEndingDP:");
    for (int i = 0; i < methods.length; i++) {
      final Method method = methods[i];
      final MatchArrayEndingTest.Expectation annotation =
          method.getAnnotation(MatchArrayEndingTest.Expectation.class);
      if (annotation != null) {
        final String provider = annotation.expectationProvider();
        final int flag = annotation.flag();
        Assert.assertNotNull(provider);
        final Method providerMethod = MatchArrayEndingTest.class.getMethod(provider, int.class);
        final Object out = providerMethod.invoke(MatchArrayEndingTest.class, flag);
        Assert.assertTrue(out instanceof Object[][]);
        log.debug(method.getName() + ", " + out);
        objects.add(new Object[] {out, method});
      }
    }
    return objects.toArray(new Object[objects.size()][]);
  }

  @DataProvider
  public Object[][] testContexts() {
    return new Object[][] {
      {TestRunner.class}, {ITestContext.class}, {TestContextJustForTesting.class}
    };
  }

  @DataProvider
  public Object[][] notTestContexts() {
    return new Object[][] {{Object.class}, {Class.class}, {Connection.class}};
  }

  @Test(dataProvider = "matchArrayEndingDP")
  public void matchArrayEndingTest(final Object[][] expected, @NoInjection final Method method) {
    if (expected != null) {
      final Parameter[] methodParameters = ReflectionRecipes.getMethodParameters(method);
      Assert.assertNotNull(methodParameters);
      final Parameter[] filteredParameters =
          ReflectionRecipes.filter(methodParameters, InjectableParameter.Assistant.ALL_INJECTS);
      Assert.assertNotNull(filteredParameters);

      for (final Object[] expect : expected) {
        Assert.assertNotNull(expect);
        Assert.assertEquals(expect.length, 2);
        Assert.assertNotNull(expect[0]);
        Assert.assertTrue(expect[0] instanceof Object[]);
        Assert.assertNotNull(expect[1]);
        Assert.assertTrue(expect[1] instanceof Boolean);
        Assert.assertEquals(
            ReflectionRecipes.matchArrayEnding(filteredParameters, (Object[]) expect[0]),
            expect[1]);
      }
    }
  }

  @Test(dataProvider = "exactMatchDP")
  public void exactMatchTest(final Object[][] expected, @NoInjection final Method method) {
    if (expected != null) {
      final Parameter[] methodParameters = ReflectionRecipes.getMethodParameters(method);
      Assert.assertNotNull(methodParameters);
      final Parameter[] filteredParameters =
          ReflectionRecipes.filter(methodParameters, InjectableParameter.Assistant.ALL_INJECTS);
      Assert.assertNotNull(filteredParameters);

      for (final Object[] expect : expected) {
        Assert.assertNotNull(expect);
        Assert.assertEquals(expect.length, 2);
        Assert.assertNotNull(expect[0]);
        Assert.assertTrue(expect[0] instanceof Object[]);
        Assert.assertNotNull(expect[1]);
        Assert.assertTrue(expect[1] instanceof Boolean);
        Assert.assertEquals(
            ReflectionRecipes.exactMatch(filteredParameters, (Object[]) expect[0]), expect[1]);
      }
    }
  }

  @Test(dataProvider = "methodInputParameters")
  public void testFilters(final Parameter[] parameters) {
    log.debug("In: " + Arrays.asList(parameters));
    final Parameter[] parameters1 =
        ReflectionRecipes.filter(parameters, InjectableParameter.Assistant.ALL_INJECTS);
    log.debug("Out: " + Arrays.asList(parameters1));
    Assert.assertEquals(parameters1.length, 2);
    Assert.assertEquals(parameters1[0].getType(), int.class);
    Assert.assertEquals(parameters1[1].getType(), Boolean.class);
  }

  @Test(dataProvider = "methodInputParamArgsPair")
  public void testInject(final Parameter[] parameters, final Object[] args) {
    log.debug("In: " + Arrays.asList(parameters));
    log.debug("args: " + Arrays.asList(args));
    final Object[] injectedArgs =
        ReflectionRecipes.inject(
            parameters, InjectableParameter.Assistant.ALL_INJECTS, args, (Method) null, null, null);
    log.debug("injectedArgs: " + Arrays.asList(injectedArgs));
    Assert.assertEquals(injectedArgs.length, parameters.length);
  }

  @Test(dataProvider = "testContexts")
  public void testIsOrImplementsInterface(final Class<?> clazz) {
    Assert.assertTrue(ReflectionRecipes.isOrImplementsInterface(ITestContext.class, clazz));
  }

  @Test(dataProvider = "notTestContexts")
  public void testNegativeCaseIsOrImplementsInterface(final Class<?> clazz) {
    Assert.assertFalse(ReflectionRecipes.isOrImplementsInterface(ITestContext.class, clazz));
  }

  private static interface T {
    public void s0(TestContextJustForTesting testContext, int i, Boolean b);

    public void s1(int i, ITestContext iTestContext, Boolean b);

    public void s2(int i, Boolean b, ITestContext iTestContext);

    public void s3(ITestContext iTestContext1, int i, Boolean b, ITestContext iTestContext2);
  }

  public abstract static class ExactMatchTest {
    public static Object[][] exactMatchData(final int flag) {
      switch (flag) {
        case 0:
          return new Object[][] {
            new Object[] {new Object[] {}, true},
            new Object[] {new Object[] {1}, false},
            new Object[] {new Object[] {""}, false},
            new Object[] {new Object[] {1, ""}, false},
          };
        case 1:
          return new Object[][] {
            new Object[] {new Object[] {1}, true},
            new Object[] {new Object[] {}, false},
            new Object[] {new Object[] {""}, false},
            new Object[] {new Object[] {1, ""}, false},
            new Object[] {new Object[] {"", 1}, false},
          };
        default:
          return null;
      }
    }

    @Expectation(expectationProvider = "exactMatchData", flag = 0)
    public abstract void s0();

    @Expectation(expectationProvider = "exactMatchData", flag = 0)
    public abstract void s0(final ITestContext a0);

    @Expectation(expectationProvider = "exactMatchData", flag = 0)
    public abstract void s0(final ITestContext a0, final ITestResult a1);

    @Expectation(expectationProvider = "exactMatchData", flag = 0)
    public abstract void s0(final ITestContext a0, final ITestResult a1, final XmlTest a2);

    @Expectation(expectationProvider = "exactMatchData", flag = 0)
    public abstract void s0(
        final ITestContext a0, final ITestResult a1, final XmlTest a2, final Method a3);

    @Expectation(expectationProvider = "exactMatchData", flag = 1)
    public abstract void s1(final int a0);

    @Expectation(expectationProvider = "exactMatchData", flag = 1)
    public abstract void s1(final ITestContext a0, final int a1);

    @Expectation(expectationProvider = "exactMatchData", flag = 1)
    public abstract void s1(final ITestContext a0, final Integer a1, final ITestResult a2);

    @Expectation(expectationProvider = "exactMatchData", flag = 1)
    public abstract void s1(
        final int a0, final ITestContext a1, final ITestResult a2, final XmlTest a3);

    @Expectation(expectationProvider = "exactMatchData", flag = 1)
    public abstract void s1(
        final ITestContext a0,
        final ITestResult a1,
        final int a2,
        final XmlTest a3,
        final Method a4);

    @Retention(RUNTIME)
    @Target({METHOD})
    public static @interface Expectation {
      public String expectationProvider();

      public int flag();
    }
  }

  public abstract static class MatchArrayEndingTest {
    public static Object[][] matchArrayEndingData(final int flag) {
      switch (flag) {
        case 0:
          return new Object[][] {
            new Object[] {new Object[] {10f, 2.1f}, true},
            new Object[] {new Object[] {10}, true},
            new Object[] {new Object[] {10d, ""}, false},
            new Object[] {new Object[] {1, ""}, false},
          };
        case 1:
          return new Object[][] {
            new Object[] {new Object[] {1, 10f, 2.1f}, true},
            new Object[] {new Object[] {}, false},
            new Object[] {new Object[] {""}, false},
            new Object[] {new Object[] {10f, "", 2.1f}, false},
            new Object[] {new Object[] {"", 10f, 2.1f}, false},
          };
        default:
          return null;
      }
    }

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 0)
    public abstract void s0(final float... f);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 0)
    public abstract void s0(final float[] f, final ITestContext a0);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 0)
    public abstract void s0(final ITestContext a0, final float[] f, final ITestResult a1);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 0)
    public abstract void s0(
        final ITestContext a0, final ITestResult a1, final XmlTest a2, final float... f);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 0)
    public abstract void s0(
        final ITestContext a0,
        final ITestResult a1,
        final XmlTest a2,
        final float[] f,
        final Method a3);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 1)
    public abstract void s1(final int a0, final float... f);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 1)
    public abstract void s1(final ITestContext a0, final int a1, final float... f);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 1)
    public abstract void s1(
        final ITestContext a0, final Integer a1, final ITestResult a2, final float... f);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 1)
    public abstract void s1(
        final int a0,
        final ITestContext a1,
        final ITestResult a2,
        final float[] f,
        final XmlTest a3);

    @Expectation(expectationProvider = "matchArrayEndingData", flag = 1)
    public abstract void s1(
        final ITestContext a0,
        final ITestResult a1,
        final int a2,
        final XmlTest a3,
        final float[] f,
        final Method a4);

    @Retention(RUNTIME)
    @Target({METHOD})
    public static @interface Expectation {
      public String expectationProvider();

      public int flag();
    }
  }
}
