package org.testng.internal;

import org.testng.IClass;
import org.testng.IObjectFactory;
import org.testng.ITest;
import org.testng.collections.Lists;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Map;

/**
 * Implementation of an IClass.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ClassImpl implements IClass {
  transient private Class m_class = null;
  transient private Object m_defaultInstance = null;
  private XmlTest m_xmlTest = null;
  transient private IAnnotationFinder m_annotationFinder = null;
  transient private List<Object> m_instances = Lists.newArrayList();
  transient private Map<Class, IClass> m_classes = null;
  private int m_instanceCount;
  private long[] m_instanceHashCodes;
  private Object m_instance;
  private IObjectFactory m_objectFactory;
  private String m_testName = null;

  public ClassImpl(Class cls, Object instance, Map<Class, IClass> classes,
      XmlTest xmlTest, IAnnotationFinder annotationFinder, IObjectFactory objectFactory) 
  {
    m_class = cls;
    m_classes = classes;
    m_xmlTest = xmlTest;
    m_annotationFinder = annotationFinder;
    m_instance = instance;
    m_objectFactory = objectFactory;
    if (instance instanceof ITest) {
      m_testName = ((ITest) instance).getTestName();
    }
  }
  
  private static void ppp(String s) {
    System.out.println("[ClassImpl] " + s);
  }

  public String getTestName() {
    return m_testName;
  }

  public String getName() {
    return m_class.getName();
  }

  public Class getRealClass() {
    return m_class;
  }

  public int getInstanceCount() {
    return m_instanceCount;
  }
  
  public long[] getInstanceHashCodes() {
    return m_instanceHashCodes;
  }
  
  private Object getDefaultInstance() {
    if (m_defaultInstance == null) {
      m_defaultInstance = 
        m_instance != null ? m_instance : 
          ClassHelper.createInstance(m_class, m_classes, m_xmlTest, m_annotationFinder, m_objectFactory);
    }
    
    return m_defaultInstance;
  }
  
  public Object[] getInstances(boolean create) {
    Object[] result = {};
    
    if (m_xmlTest.isJUnit()) {
      if (create) {
        result = new Object[] { 
          ClassHelper.createInstance(m_class, m_classes, m_xmlTest, m_annotationFinder, m_objectFactory) 
        };
      }
    }
    else {
      result = new Object[] { getDefaultInstance() };
    }
    if (m_instances.size() > 0) {
      result = m_instances.toArray(new Object[m_instances.size()]);
    }

    m_instanceCount = m_instances.size();
    m_instanceHashCodes = new long[m_instanceCount];
    for (int i = 0; i < m_instanceCount; i++) {
      m_instanceHashCodes[i] = m_instances.get(i).hashCode();
    }
    return result;
  }
    
  @Override
  public String toString() {
    return "[ClassImpl " + m_class.getName() + "]";
  }

  public void addInstance(Object instance) {
    m_instances.add(instance);
  }

}
