package org.testng.internal;

import java.util.Map;
import org.testng.IClass;
import org.testng.ITestClassFinder;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.collections.Maps;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;

/**
 * This class
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public abstract class BaseClassFinder implements ITestClassFinder {
  private final Map<Class<?>, IClass> m_classes = Maps.newLinkedHashMap();

  @Override
  public IClass getIClass(Class<?> cls) {
    return m_classes.get(cls);
  }

  protected void putIClass(Class<?> cls, IClass iClass) {
    if (!m_classes.containsKey(cls)) {
      m_classes.put(cls, iClass);
    }
  }

  protected IClass findOrCreateIClass(
      ITestContext context,
      Class<?> cls,
      XmlClass xmlClass,
      Object instance,
      IAnnotationFinder annotationFinder,
      ITestObjectFactory objectFactory) {

    return m_classes.computeIfAbsent(
        cls,
        key ->
            new ClassImpl(
                context, key, xmlClass, instance, m_classes, annotationFinder, objectFactory));
  }

  protected boolean classExists(Class<?> cls) {
    return m_classes.containsKey(cls);
  }

  @Override
  public IClass[] findTestClasses() {
    return m_classes.values().toArray(new IClass[0]);
  }
}
