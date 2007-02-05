package org.testng.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
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
  private ITestContext m_testContext = null;
  
  /**
   * @param testClass
   * @param method
   */
  public FactoryMethod(Method method, 
                       Object instance,
                       XmlTest xmlTest, 
                       IAnnotationFinder annotationFinder,
                       ITestContext testContext) 
  {
    super(method, annotationFinder);
    if (! instance.getClass().isAssignableFrom(method.getDeclaringClass())) {
      throw new TestNGException("Mismatch between instance/method classes:"
          + instance.getClass() + " " + method.getDeclaringClass());
    }
    
    m_instance = instance;
    m_xmlTest = xmlTest;
    m_testContext = testContext;
    NoOpTestClass tc = new NoOpTestClass();
    tc.setTestClass(method.getDeclaringClass());
    m_testClass = tc;
  }
  
  private static void ppp(String s) {
    System.out.println("[FactoryMethod] " + s);
  }
  
  public Object[] invoke() {
    List<Object> result = new ArrayList<Object>();
    
//    Object[] parameters = null; 
//      Parameters.createFactoryParameters(getMethod(), 
//          m_xmlTest.getParameters(), getAnnotationFinder(), m_xmlTest.getSuite());
    
    
    //
    // Temporary test for data providers on factories
    //
    Map<String, String> allParameterNames = new HashMap<String, String>();
    Iterator<Object[]> parameterIterator =
      Parameters.handleParameters(this, 
          allParameterNames, m_instance,
          m_xmlTest.getParameters(), 
          m_xmlTest.getSuite(), m_annotationFinder, 
          m_testContext);
    //
    // end
    //
    try {
      while (parameterIterator.hasNext()) {
        Object[] parameters = parameterIterator.next();
        Object[] testInstances = 
          (Object[]) getMethod().invoke(m_instance, parameters);
        for (Object testInstance : testInstances) {
          result.add(testInstance);
        }
      }
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
    
    return result.toArray(new Object[result.size()]);
  }
  
  public ITestNGMethod clone() {
    throw new IllegalStateException("clone is not supported for FactoryMethod"); 
  }
}
