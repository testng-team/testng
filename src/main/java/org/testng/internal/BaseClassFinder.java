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
abstract public class BaseClassFinder implements ITestClassFinder {
  private Map<Class, IClass> m_classes = Maps.newLinkedHashMap();

  @Override
  public IClass getIClass(Class cls) {
    return m_classes.get(cls);
  }

  protected void putIClass(Class cls, IClass iClass) {
    if (! m_classes.containsKey(cls)) {
      m_classes.put(cls, iClass);
    }
  }

  private void ppp(String s) {
    System.out.println("[BaseClassFinder] " + s);
  }

  /**
   * @param cls
   * @return An IClass for the given class, or null if we have
   * already treated this class.
   */
  protected IClass findOrCreateIClass(ITestContext context, Class cls, XmlClass xmlClass,
      Object instance, XmlTest xmlTest, IAnnotationFinder annotationFinder,
      ITestObjectFactory objectFactory)
  {
    IClass result = m_classes.get(cls);
    if (null == result) {
      result = new ClassImpl(context, cls, xmlClass, instance, m_classes, xmlTest, annotationFinder,
          objectFactory);
      m_classes.put(cls, result);
    }

    return result;
  }

  protected Map getExistingClasses() {
    return m_classes;
  }

  protected boolean classExists(Class cls) {
    return m_classes.containsKey(cls);
  }

  @Override
  public IClass[] findTestClasses() {
    return m_classes.values().toArray(new IClass[m_classes.size()]);
   }
}
