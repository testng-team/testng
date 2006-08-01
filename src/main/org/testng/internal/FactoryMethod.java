package org.testng.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.testng.TestNGException;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/**
 * This class represents a method annotated with @Factory
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class FactoryMethod extends BaseTestMethod {
  private Object m_instance = null;
  private XmlTest m_xmlTest = null;
  
  /**
   * @param testClass
   * @param method
   */
  public FactoryMethod(Method method, 
                       Object instance,
                       XmlTest xmlTest, 
                       IAnnotationFinder annotationFinder) 
  {
    super(method, annotationFinder);
    if (! instance.getClass().isAssignableFrom(method.getDeclaringClass())) {
      throw new TestNGException("Mismatch between instance/method classes:"
          + instance.getClass() + " " + method.getDeclaringClass());
    }
    
    m_instance = instance;
    m_xmlTest = xmlTest;
  }
  
  public Object[] invoke() {
    Object[] result = new Object[0];
    
    Object[] parameters = 
      Parameters.createFactoryParameters(getMethod(), 
          m_xmlTest.getParameters(), getAnnotationFinder(), m_xmlTest.getSuite());
    try {
      result = (Object[]) getMethod().invoke(m_instance, parameters);
    }
    catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    
    return result;
  }
}
