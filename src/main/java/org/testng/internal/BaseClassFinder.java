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
  //    private final Map<Class<?>, IClass> m_classes = Maps.newLinkedHashMap();
  private final Map<XmlClass, IClass> m_classes = Maps.newLinkedHashMap();

//    @Override
//    public IClass getIClass(Class<?> cls) {
//        return m_classes.get(cls);
//    }


  @Override
  public IClass getIClass(XmlClass xmlClass) {
    return m_classes.get(xmlClass);
  }

//    protected void putIClass(Class<?> cls, IClass iClass) {
//        if (!m_classes.containsKey(cls)) {
//            m_classes.put(cls, iClass);
//        }
//    }

  protected void putIClass(XmlClass xmlClass, IClass iClass) {
    if (!m_classes.containsKey(xmlClass)) {
      m_classes.put(xmlClass, iClass);
    }
  }

  /**
   * @param cls
   * @return An IClass for the given class, or null if we have
   * already treated this class.
   * @deprecated - This method stands deprecated as of TestNG v6.13
   */
  @Deprecated
  @SuppressWarnings("unused")
  protected IClass findOrCreateIClass(ITestContext context, Class<?> cls, XmlClass xmlClass,
                                      Object instance, XmlTest xmlTest, IAnnotationFinder annotationFinder,
                                      ITestObjectFactory objectFactory) {
    return findOrCreateIClass(context, cls, xmlClass, instance, annotationFinder, objectFactory);
  }

//    protected IClass findOrCreateIClass(ITestContext context, Class<?> cls, XmlClass xmlClass,
//                                        Object instance, IAnnotationFinder annotationFinder,
//                                        ITestObjectFactory objectFactory) {
//        IClass result = m_classes.get(cls);
//        if (null == result) {
//            result = new ClassImpl(context, cls, xmlClass, instance, m_classes, annotationFinder,
//                    objectFactory);
//            m_classes.put(cls, result);
//        }
//
//        return result;
//    }

  protected IClass findOrCreateIClass(ITestContext context, Class<?> cls, XmlClass xmlClass,
                                      Object instance, IAnnotationFinder annotationFinder,
                                      ITestObjectFactory objectFactory) {
    IClass result = m_classes.get(xmlClass);
    if (null == result) {
      result = new ClassImpl(context, cls, xmlClass, instance, m_classes, annotationFinder,
        objectFactory);
      m_classes.put(xmlClass, result);
    }

    return result;
  }

  @Deprecated
  protected Map getExistingClasses() {
    return m_classes;
  }

//    protected boolean classExists(Class<?> cls) {
//        return m_classes.containsKey(cls);
//    }

  protected boolean classExists(XmlClass xmlClass) {
    return m_classes.containsKey(xmlClass);
  }

  @Override
  public IClass[] findTestClasses() {
    return m_classes.values().toArray(new IClass[m_classes.size()]);
  }
}
