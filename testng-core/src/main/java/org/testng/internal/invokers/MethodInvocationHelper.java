package org.testng.internal.invokers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.MethodHelper;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.collections.ArrayIterator;
import org.testng.internal.collections.OneToTwoDimArrayIterator;
import org.testng.internal.collections.OneToTwoDimIterator;
import org.testng.internal.collections.Pair;
import org.testng.internal.invokers.InvokeMethodRunnable.TestNGRuntimeException;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.internal.thread.ThreadExecutionException;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlSuite;

/** Collections of helper methods to help deal with invocation of TestNG methods */
public class MethodInvocationHelper {

  protected static Object invokeMethodNoCheckedException(
      Method thisMethod, Object instance, List<Object> parameters) {
    try {
      return invokeMethod(thisMethod, instance, parameters);
    } catch (InvocationTargetException | IllegalAccessException e) {
      // Don't throw TestNGException here or this test won't be reported as a
      // skip or failure
      throw new RuntimeException(e.getCause());
    }
  }

  protected static void invokeMethodConsideringTimeout(
      ITestNGMethod tm,
      ConstructorOrMethod method,
      Object targetInstance,
      Object[] params,
      ITestResult testResult)
      throws Throwable {
    if (MethodHelper.calculateTimeOut(tm) <= 0) {
      MethodInvocationHelper.invokeMethod(method.getMethod(), targetInstance, params);
    } else {
      MethodInvocationHelper.invokeWithTimeout(tm, targetInstance, params, testResult);
      if (!testResult.isSuccess()) {
        // A time out happened
        Throwable ex = testResult.getThrowable();
        testResult.setStatus(ITestResult.FAILURE);
        testResult.setThrowable(ex.getCause() == null ? ex : ex.getCause());
        throw testResult.getThrowable();
      }
    }
  }

  protected static Object invokeMethod(Method thisMethod, Object instance, List<Object> parameters)
      throws InvocationTargetException, IllegalAccessException {
    return invokeMethod(thisMethod, instance, parameters.toArray(new Object[0]));
  }

  protected static Object invokeMethod(Method thisMethod, Object instance, Object[] parameters)
      throws InvocationTargetException, IllegalAccessException {
    Utils.checkInstanceOrStatic(instance, thisMethod);

    // TESTNG-326, allow IObjectFactory to load from non-standard classloader
    // If the instance has a different classloader, its class won't match the
    // method's class
    if (instance != null && !thisMethod.getDeclaringClass().isAssignableFrom(instance.getClass())) {
      // for some reason, we can't call this method on this class
      // is it static?
      boolean isStatic = Modifier.isStatic(thisMethod.getModifiers());
      if (!isStatic) {
        // not static, so grab a method with the same name and signature in this case
        Class<?> clazz = instance.getClass();
        try {
          thisMethod = clazz.getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
        } catch (Exception e) {
          // ignore, the method may be private
          boolean found = false;
          for (; clazz != null; clazz = clazz.getSuperclass()) {
            try {
              thisMethod =
                  clazz.getDeclaredMethod(thisMethod.getName(), thisMethod.getParameterTypes());
              found = true;
              break;
            } catch (Exception e2) {
            }
          }
          if (!found) {
            // should we assert here? Or just allow it to fail on invocation?
            if (thisMethod.getDeclaringClass().equals(instance.getClass())) {
              throw new RuntimeException(
                  "Can't invoke method " + thisMethod + ", probably due to classloader mismatch");
            }
            throw new RuntimeException(
                "Can't invoke method "
                    + thisMethod
                    + " on this instance of "
                    + instance.getClass()
                    + " due to class mismatch");
          }
        }
      }
    }

    if (!Modifier.isPublic(thisMethod.getModifiers()) || !canAccess(thisMethod)) {
      try {
        thisMethod.setAccessible(true);
      } catch (SecurityException e) {
        throw new TestNGException(thisMethod.getName() + " must be public", e);
      }
    }
    return thisMethod.invoke(instance, parameters);
  }

  @SuppressWarnings("deprecation")
  private static boolean canAccess(Method thisMethod) {
    return thisMethod.isAccessible();
  }

  @SuppressWarnings("unchecked")
  public static Iterator<Object[]> invokeDataProvider(
      Object instance,
      Method dataProvider,
      ITestNGMethod method,
      ITestContext testContext,
      Object fedInstance,
      IAnnotationFinder annotationFinder) {
    List<Object> parameters =
        getParameters(dataProvider, method, testContext, fedInstance, annotationFinder);
    Object result = invokeMethodNoCheckedException(dataProvider, instance, parameters);
    if (result == null) {
      throw new TestNGException("Data Provider " + dataProvider + " returned a null value");
    }
    // If it returns an Object[][] or Object[], convert it to an Iterator<Object[]>
    if (result instanceof Object[][]) {
      return new ArrayIterator((Object[][]) result);
    } else if (result instanceof Object[]) {
      return new OneToTwoDimArrayIterator((Object[]) result);
    } else if (result instanceof Iterator) {
      Type returnType = dataProvider.getGenericReturnType();
      if (returnType instanceof ParameterizedType) {
        ParameterizedType contentType = (ParameterizedType) returnType;
        Type actualType = contentType.getActualTypeArguments()[0];
        Class<?> type;
        if (actualType instanceof ParameterizedType) {
          type = (Class<?>) ((ParameterizedType) actualType).getActualTypeArguments()[0];
        } else {
          type = (Class<?>) actualType;
        }
        if (type.isArray()) {
          return (Iterator<Object[]>) result;
        } else {
          return new OneToTwoDimIterator((Iterator<Object>) result);
        }
      } else {
        // Raw Iterator, we expect user provides the expected type
        return (Iterator<Object[]>) result;
      }
    }
    throw new TestNGException(
        "Data Provider "
            + dataProvider
            + " must return"
            + " either Object[][] or Object[] or Iterator<Object[]> or Iterator<Object>, not "
            + dataProvider.getReturnType());
  }

  private static List<Object> getParameters(
      Method dataProvider,
      ITestNGMethod method,
      ITestContext testContext,
      Object fedInstance,
      IAnnotationFinder annotationFinder) {
    // Go through all the parameters declared on this Data Provider and
    // make sure we have at most one Method and one ITestContext.
    // Anything else is an error
    List<Object> parameters = new ArrayList<>();
    Collection<Pair<Integer, Class<?>>> unresolved = new ArrayList<>();
    ConstructorOrMethod com = method.getConstructorOrMethod();
    int i = 0;
    for (Class<?> cls : dataProvider.getParameterTypes()) {
      if (cls.equals(Method.class)) {
        parameters.add(com.getMethod());
      } else if (cls.equals(Constructor.class)) {
        parameters.add(com.getConstructor());
      } else if (cls.equals(ConstructorOrMethod.class)) {
        parameters.add(com);
      } else if (cls.equals(ITestNGMethod.class)) {
        parameters.add(method);
      } else if (cls.equals(ITestContext.class)) {
        parameters.add(testContext);
      } else if (cls.equals(Class.class)) {
        parameters.add(com.getDeclaringClass());
      } else {
        boolean isTestInstance = annotationFinder.hasTestInstance(dataProvider, i);
        if (isTestInstance) {
          parameters.add(fedInstance);
        } else {
          unresolved.add(new Pair<>(i, cls));
        }
      }
      i++;
    }
    if (!unresolved.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Some DataProvider ").append(dataProvider).append(" parameters unresolved: ");
      for (Pair<Integer, Class<?>> pair : unresolved) {
        sb.append(" at ").append(pair.first()).append(" type ").append(pair.second()).append("\n");
      }
      throw new TestNGException(sb.toString());
    }
    return parameters;
  }

  protected static void invokeHookable(
      final Object testInstance,
      final Object[] parameters,
      final IHookable hookable,
      final Method thisMethod,
      final ITestResult testResult)
      throws Throwable {
    final Throwable[] error = new Throwable[1];

    IHookCallBack callback =
        new IHookCallBack() {
          @Override
          public void runTestMethod(ITestResult tr) {
            try {
              invokeMethod(thisMethod, testInstance, parameters);
              error[0] = null;
              tr.setThrowable(null);
            } catch (Throwable t) {
              error[0] = t;
              tr.setThrowable(t); // make Throwable available to IHookable
            }
          }

          @Override
          public Object[] getParameters() {
            return parameters;
          }
        };
    hookable.run(callback, testResult);
    if (error[0] != null) {
      throw error[0];
    }
  }

  /**
   * Invokes a method on a separate thread in order to allow us to timeout the invocation. It uses
   * as implementation an <code>Executor</code> and a <code>CountDownLatch</code>.
   */
  protected static void invokeWithTimeout(
      ITestNGMethod tm, Object instance, Object[] parameterValues, ITestResult testResult)
      throws InterruptedException, ThreadExecutionException {
    invokeWithTimeout(tm, instance, parameterValues, testResult, null);
  }

  protected static void invokeWithTimeout(
      ITestNGMethod tm,
      Object instance,
      Object[] parameterValues,
      ITestResult testResult,
      IHookable hookable)
      throws InterruptedException, ThreadExecutionException {
    if (ThreadUtil.isTestNGThread()
        && testResult.getTestContext().getCurrentXmlTest().getParallel()
            != XmlSuite.ParallelMode.TESTS) {
      // We are already running in our own executor, don't create another one (or we will
      // lose the time out of the enclosing executor).
      invokeWithTimeoutWithNoExecutor(tm, instance, parameterValues, testResult, hookable);
    } else {
      invokeWithTimeoutWithNewExecutor(tm, instance, parameterValues, testResult, hookable);
    }
  }

  private static void invokeWithTimeoutWithNoExecutor(
      ITestNGMethod tm,
      Object instance,
      Object[] parameterValues,
      ITestResult testResult,
      IHookable hookable) {

    InvokeMethodRunnable imr =
        new InvokeMethodRunnable(tm, instance, parameterValues, hookable, testResult);
    long startTime = System.currentTimeMillis();
    long realTimeOut = MethodHelper.calculateTimeOut(tm);
    boolean notTimedout = true;
    AtomicBoolean finished = new AtomicBoolean(false);
    AtomicBoolean interruptByMonitor = new AtomicBoolean(false);
    Thread monitorThread = null;
    try {
      Thread currentThread = Thread.currentThread();
      monitorThread =
          new Thread(
              () -> {
                try {
                  TimeUnit.MILLISECONDS.sleep(realTimeOut);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
                if (!finished.get()) {
                  interruptByMonitor.set(true);
                  currentThread.interrupt();
                }
              });
      monitorThread.start();
      imr.run();
      notTimedout = System.currentTimeMillis() <= startTime + realTimeOut;
      if (notTimedout) {
        testResult.setStatus(ITestResult.SUCCESS);
      } else {
        testResult.setThrowable(new ThreadTimeoutException(tm, realTimeOut));
        testResult.setStatus(ITestResult.FAILURE);
      }
    } catch (Exception ex) {
      if (notTimedout && !interruptByMonitor.get()) {
        Throwable e = ex.getCause();
        if (e instanceof TestNGRuntimeException) {
          e = e.getCause();
        }
        testResult.setThrowable(e);
      } else {
        testResult.setThrowable(new ThreadTimeoutException(tm, realTimeOut));
      }
      testResult.setStatus(ITestResult.FAILURE);
    } finally {
      finished.set(true);
      if (monitorThread != null && monitorThread.isAlive()) {
        monitorThread.interrupt();
      }
    }
  }

  private static void invokeWithTimeoutWithNewExecutor(
      ITestNGMethod tm,
      Object instance,
      Object[] parameterValues,
      ITestResult testResult,
      IHookable hookable)
      throws InterruptedException, ThreadExecutionException {
    ExecutorService exec = ThreadUtil.createExecutor(1, tm.getMethodName());

    InvokeMethodRunnable imr =
        new InvokeMethodRunnable(tm, instance, parameterValues, hookable, testResult);
    Future<Void> future = exec.submit(imr);
    exec.shutdown();
    long realTimeOut = MethodHelper.calculateTimeOut(tm);
    boolean finished = exec.awaitTermination(realTimeOut, TimeUnit.MILLISECONDS);

    if (!finished) {
      ThreadTimeoutException exception = new ThreadTimeoutException(tm, realTimeOut);
      StackTraceElement[] realStackTrace = getRunningMethodStackTrace(exec);
      if (realStackTrace != null) {
        exception.setStackTrace(realStackTrace);
      }
      // shutdown after getting real stacktrace from thread
      exec.shutdownNow();
      testResult.setThrowable(exception);
      testResult.setStatus(ITestResult.FAILURE);
    } else {
      Utils.log(
          "Invoker " + Thread.currentThread().hashCode(),
          3,
          "Method " + tm.getMethodName() + " completed within the time-out " + tm.getTimeOut());

      // We don't need the result from the future but invoking get() on it
      // will trigger the exception that was thrown, if any
      try {
        future.get();
      } catch (ExecutionException e) {
        throw new ThreadExecutionException(e.getCause());
      }

      testResult.setStatus(ITestResult.SUCCESS); // if no exception till here then SUCCESS.
    }
  }

  private static StackTraceElement[] getRunningMethodStackTrace(ExecutorService exec) {
    if (!(exec instanceof ThreadPoolExecutor)) {
      return null;
    }
    ThreadFactory threadFactory = ((ThreadPoolExecutor) exec).getThreadFactory();

    if (!(threadFactory instanceof TestNGThreadFactory)) {
      return null;
    }
    TestNGThreadFactory testNGThreadFactory = (TestNGThreadFactory) threadFactory;

    StackTraceElement[] stackTrace = null;
    Collection<Thread> threads = testNGThreadFactory.getRunningThreads();
    if (threads.size() == 1) {
      stackTrace = threads.iterator().next().getStackTrace();
    } else if (threads.size() > 1) {
      Utils.warn("Cannot resolve stacktrace from multiple threads");
    }
    return stackTrace;
  }

  protected static void invokeConfigurable(
      final Object instance,
      final Object[] parameters,
      final IConfigurable configurableInstance,
      final Method thisMethod,
      final ITestResult testResult)
      throws Throwable {
    final Throwable[] error = new Throwable[1];

    IConfigureCallBack callback =
        new IConfigureCallBack() {
          @Override
          public void runConfigurationMethod(ITestResult tr) {
            try {
              invokeMethod(thisMethod, instance, parameters);
              error[0] = null;
              tr.setThrowable(null);
            } catch (Throwable t) {
              error[0] = t;
              tr.setThrowable(t); // make Throwable available to IConfigurable
            }
          }

          @Override
          public Object[] getParameters() {
            return parameters;
          }
        };
    configurableInstance.run(callback, testResult);
    if (error[0] != null) {
      throw error[0];
    }
  }
}
