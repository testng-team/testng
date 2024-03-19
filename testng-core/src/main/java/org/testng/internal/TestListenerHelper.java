package org.testng.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.testng.IClass;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.ITestResult;
import org.testng.ListenerComparator;
import org.testng.TestNGException;
import org.testng.annotations.IListenersAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;

/** A helper class that internally houses some of the listener related actions support. */
public final class TestListenerHelper {
  private TestListenerHelper() {
    // Utility class. Defeat instantiation.
  }

  public static void runPreConfigurationListeners(
      ITestResult tr,
      ITestNGMethod tm,
      List<IConfigurationListener> listeners,
      IConfigurationListener internal,
      ListenerComparator comparator) {
    internal.beforeConfiguration(tr);
    List<IConfigurationListener> original = ListenerOrderDeterminer.order(listeners, comparator);

    for (IConfigurationListener icl : original) {
      icl.beforeConfiguration(tr);
      try {
        icl.beforeConfiguration(tr, tm);
      } catch (Exception e) {
        ignoreInternalGradleException(e);
      }
    }
  }

  public static void runPostConfigurationListeners(
      ITestResult tr,
      ITestNGMethod tm,
      List<IConfigurationListener> listeners,
      IConfigurationListener internal,
      ListenerComparator comparator) {
    List<IConfigurationListener> listenersreversed =
        ListenerOrderDeterminer.reversedOrder(listeners, comparator);
    listenersreversed.add(internal);
    for (IConfigurationListener icl : listenersreversed) {
      switch (tr.getStatus()) {
        case ITestResult.SKIP:
          icl.onConfigurationSkip(tr);
          try {
            icl.onConfigurationSkip(tr, tm);
          } catch (Exception e) {
            ignoreInternalGradleException(e);
          }
          break;
        case ITestResult.FAILURE:
          icl.onConfigurationFailure(tr);
          try {
            icl.onConfigurationFailure(tr, tm);
          } catch (Exception e) {
            ignoreInternalGradleException(e);
          }
          break;
        case ITestResult.SUCCESS:
          icl.onConfigurationSuccess(tr);
          try {
            icl.onConfigurationSuccess(tr, tm);
          } catch (Exception e) {
            ignoreInternalGradleException(e);
          }
          break;
        default:
          throw new AssertionError("Unexpected value: " + tr.getStatus());
      }
    }
  }

  // This method is added because Gradle which builds TestNG seems to be using an older version
  // of TestNG that doesn't know about the new methods that we added and so it causes
  // the TestNG build to keep failing.
  private static void ignoreInternalGradleException(Exception e) {
    if (!e.getClass().getPackage().getName().startsWith("org.gradle.internal")) {
      throw new ListenerInvocationException(e);
    }
  }

  /**
   * Iterates through a bunch of listeners and invokes them.
   *
   * @param tr - The {@link ITestResult} object that is to be fed into a listener when invoking it.
   * @param listeners - A list of {@link ITestListener} objects which are to be invoked.
   */
  public static void runTestListeners(ITestResult tr, List<ITestListener> listeners) {
    for (ITestListener itl : listeners) {
      switch (tr.getStatus()) {
        case ITestResult.SKIP:
          itl.onTestSkipped(tr);
          break;
        case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
          itl.onTestFailedButWithinSuccessPercentage(tr);
          break;
        case ITestResult.FAILURE:
          if (ITestResult.wasFailureDueToTimeout(tr)) {
            itl.onTestFailedWithTimeout(tr);
          } else {
            itl.onTestFailure(tr);
          }
          break;
        case ITestResult.SUCCESS:
          itl.onTestSuccess(tr);
          break;
        case ITestResult.STARTED:
          itl.onTestStart(tr);
          break;
        default:
          throw new AssertionError("Unknown status: " + tr.getStatus());
      }
    }
  }

  /**
   * @return all the @Listeners annotations found in the current class and its superclasses and
   *     inherited interfaces.
   */
  public static ListenerHolder findAllListeners(Class<?> cls, IAnnotationFinder finder) {
    ListenerHolder result = new ListenerHolder();

    while (cls != Object.class) {
      List<IListenersAnnotation> annotations =
          finder.findInheritedAnnotations(cls, IListenersAnnotation.class);
      Optional.ofNullable(finder.findAnnotation(cls, IListenersAnnotation.class))
          .ifPresent(annotations::add);

      annotations.stream().flatMap(it -> Arrays.stream(it.getValue())).forEach(result::addListener);
      cls = cls.getSuperclass();
    }
    return result;
  }

  /** @deprecated - This method stands deprecated as of TestNG version <code>7.10.0</code> */
  @Deprecated
  public static ITestNGListenerFactory createListenerFactory(
      ITestObjectFactory objectFactory,
      TestNGClassFinder finder,
      Class<? extends ITestNGListenerFactory> factoryClass,
      ITestContext context) {
    ITestNGListenerFactory listenerFactory = null;
    try {
      if (finder != null) {
        IClass ic = finder.getIClass(factoryClass);
        IObject.IdentifiableObject[] created = IObject.objects(ic, false);
        if (created.length != 0) {
          listenerFactory = (ITestNGListenerFactory) created[0].getInstance();
        }
      }
      if (listenerFactory == null) {
        if (DefaultListenerFactory.class.equals(factoryClass)) {
          listenerFactory = new DefaultListenerFactory(objectFactory, context);
        } else {
          listenerFactory = factoryClass != null ? objectFactory.newInstance(factoryClass) : null;
        }
      }
      return listenerFactory;
    } catch (Exception ex) {
      throw new TestNGException("Couldn't instantiate the ITestNGListenerFactory: " + ex, ex);
    }
  }

  public static class ListenerHolder {
    private final List<Class<? extends ITestNGListener>> listenerClasses = new ArrayList<>();
    private Class<? extends ITestNGListenerFactory> listenerFactoryClass;

    @SuppressWarnings("unchecked")
    public void addListener(Class<? extends ITestNGListener> c) {
      // @Listener annotation is now inheritable. So let's add it ONLY
      // if it wasn't added already
      if (!listenerClasses.contains(c)) {
        listenerClasses.add(c);
      }
      if (ITestNGListenerFactory.class.isAssignableFrom(c)) {
        setListenerFactoryClass((Class<? extends ITestNGListenerFactory>) c);
      }
    }

    private void setListenerFactoryClass(Class<? extends ITestNGListenerFactory> c) {
      if (c.equals(listenerFactoryClass)) {
        return;
      }
      if (listenerFactoryClass != null) {
        // Let's say we already know of a ListenerFactoryClass called `MyFactory`.
        // Now we are stumbling into another ListenerFactoryClass called `YourFactory`
        // We need to throw an exception, because we can ONLY deal with 1 listener factory.
        throw new TestNGException(
            "Found more than one class implementing ITestNGListenerFactory:"
                + c
                + " and "
                + listenerFactoryClass);
      }
      listenerFactoryClass = c;
    }

    public List<Class<? extends ITestNGListener>> getListenerClasses() {
      return listenerClasses;
    }

    public Class<? extends ITestNGListenerFactory> getListenerFactoryClass() {
      return listenerFactoryClass;
    }
  }

  static class ListenerInvocationException extends RuntimeException {

    public ListenerInvocationException(Throwable cause) {
      super(cause);
    }
  }
}
