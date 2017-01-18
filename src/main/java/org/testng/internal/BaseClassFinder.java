package org.testng.internal;

import org.testng.IClass;
import org.testng.ITestClassFinder;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.collections.Maps;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.util.Map;

/**
 * This class
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public abstract class BaseClassFinder implements ITestClassFinder {
  private final Map<Class<?>, IClass> classes = Maps.newLinkedHashMap();

  @Override
  public IClass getIClass(Class<?> cls) {
    return classes.get(cls);
  }

  protected void putIClass(Class<?> cls, IClass iClass) {
    if (! classes.containsKey(cls)) {
      classes.put(cls, iClass);
    }
  }

  /**
   * @param cls
   * @return An IClass for the given class, or null if we have
   * already treated this class.
   */
  protected IClass findOrCreateIClass(ITestContext context, Class<?> cls, XmlClass xmlClass,
      Object instance, XmlTest xmlTest, IAnnotationFinder annotationFinder,
      ITestObjectFactory objectFactory)
  {
    IClass result = classes.get(cls);
    if (null == result) {
      result = new ClassImpl(context, cls, xmlClass, instance, classes, xmlTest, annotationFinder,
          objectFactory);
      classes.put(cls, result);
    }

    return result;
  }

  @Deprecated
  protected Map getExistingClasses() {
    return classes;
  }

  protected boolean classExists(Class<?> cls) {
    return classes.containsKey(cls);
  }

  @Override
  public IClass[] findTestClasses() {
    return classes.values().toArray(new IClass[classes.size()]);
   }
}
