package org.testng.internal.annotations;

import org.testng.internal.annotations.IAnnotationFinder;

/**
 * This class is responsible for returning the correct IAnnotationFinder.
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 *
 * TODO: to be removed 
 */
public class AnnotationConfiguration {
  public static final int JVM_15_CONFIG = 1;
  public static final int JVM_14_CONFIG = 2;
  
  private IAnnotationFinder m_jdk14Finder = null;
  private IAnnotationFinder m_jdk15Finder = null;
  
  private static AnnotationConfiguration m_instance = new AnnotationConfiguration();

//  public static AnnotationConfiguration getInstance() {
//    return m_instance;
//  }

  private int m_annotationType= JVM_14_CONFIG;
  
//  public IAnnotationFinder getAnnotationFinder(IAnnotationTransformer transformer) {
//    IAnnotationFinder result = null;
//    
//    if (m_annotationType == JVM_15_CONFIG) {
//      if (m_jdk15Finder == null) {
//        m_jdk15Finder = new JDK15AnnotationFinder(transformer);
//      }
//      result = m_jdk15Finder;
//    }
//    else {
//      if (m_jdk14Finder == null) {
//        m_jdk14Finder = new JDK14AnnotationFinder(transformer);
//      }
//      result = m_jdk14Finder;
//    }
//    
//    return result;
//  }
  
//  public IAnnotationFinder getJavadocAnnotationFinder() {
//    return m_jdk14Finder;
//  }  

//  public void initialize(int annotationType) {
//    m_annotationType = annotationType;
//  }
}
