package org.testng.internal.annotations;

import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK14AnnotationFinder;

public class AnnotationConfiguration {
  public static final int JVM_15_CONFIG = 1;
  public static final int JVM_14_CONFIG = 2;

  private JDK14AnnotationFinder m_finder = new JDK14AnnotationFinder();
  private static AnnotationConfiguration m_instance = new AnnotationConfiguration();

  public static AnnotationConfiguration getInstance() {
    return m_instance;
  }
  
  public void initialize(int annotationType) {
    // Ignore:  only 1.4 is supported in this file
  }  

  public IAnnotationFinder getAnnotationFinder() {
    return m_finder;
  }
  
  public JDK14AnnotationFinder getJavadocAnnotationFinder() {
    return m_finder;
  }  
}
