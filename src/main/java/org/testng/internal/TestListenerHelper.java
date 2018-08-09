package org.testng.internal;

import org.testng.IClass;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.annotations.IListenersAnnotation;
import org.testng.collections.Lists;
import org.testng.internal.annotations.IAnnotationFinder;

import java.util.List;

/** A helper class that internally houses some of the listener related actions support. */
public final class TestListenerHelper {
  private TestListenerHelper() {
    // Utility class. Defeat instantiation.
  }

  static void runPreConfigurationListeners(ITestResult tr, List<IConfigurationListener> listeners) {
    for (IConfigurationListener icl : listeners) {
      icl.beforeConfiguration(tr);
    }
  }

  static void runPostConfigurationListeners(
      ITestResult tr, List<IConfigurationListener> listeners) {
    for (IConfigurationListener icl : listeners) {
      switch (tr.getStatus()) {
        case ITestResult.SKIP:
          icl.onConfigurationSkip(tr);
          break;
        case ITestResult.FAILURE:
          icl.onConfigurationFailure(tr);
          break;
        case ITestResult.SUCCESS:
          icl.onConfigurationSuccess(tr);
          break;
        default:
          throw new AssertionError("Unexpected value: " + tr.getStatus());
      }
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
          itl.onTestFailure(tr);
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

  /** @return all the @Listeners annotations found in the current class and its superclasses. */
  public static ListenerHolder findAllListeners(Class<?> cls, IAnnotationFinder finder) {
    ListenerHolder result = new ListenerHolder();
    result.listenerClasses = Lists.newArrayList();

    while (cls != Object.class) {
      IListenersAnnotation l = finder.findAnnotation(cls, IListenersAnnotation.class);
      if (l != null) {
        Class<? extends ITestNGListener>[] classes = l.getValue();
        for (Class<? extends ITestNGListener> c : classes) {
          result.listenerClasses.add(c);

          if (ITestNGListenerFactory.class.isAssignableFrom(c)) {
            if (result.listenerFactoryClass == null) {
              result.listenerFactoryClass = (Class<? extends ITestNGListenerFactory>) c;
            } else {
              throw new TestNGException(
                  "Found more than one class implementing "
                      + "ITestNGListenerFactory:"
                      + c
                      + " and "
                      + result.listenerFactoryClass);
            }
          }
        }
      }
      cls = cls.getSuperclass();
    }
    return result;
  }

  public static ITestNGListenerFactory createListenerFactory(
      TestNGClassFinder finder, Class<? extends ITestNGListenerFactory> factoryClass) {
    ITestNGListenerFactory listenerFactory = null;
    try {
      if (finder != null) {
        IClass ic = finder.getIClass(factoryClass);
        if (ic != null) {
          listenerFactory = (ITestNGListenerFactory) ic.getInstances(false)[0];
        }
      }
      if (listenerFactory == null) {
        listenerFactory = factoryClass != null ? factoryClass.newInstance() : null;
      }
      return listenerFactory;
    } catch (Exception ex) {
      throw new TestNGException("Couldn't instantiate the ITestNGListenerFactory: " + ex);
    }
  }

  public static class ListenerHolder {
    private List<Class<? extends ITestNGListener>> listenerClasses;
    private Class<? extends ITestNGListenerFactory> listenerFactoryClass;

    public List<Class<? extends ITestNGListener>> getListenerClasses() {
      return listenerClasses;
    }

    public Class<? extends ITestNGListenerFactory> getListenerFactoryClass() {
      return listenerFactoryClass;
    }
  }
}
