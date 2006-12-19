package org.testng.internal;

import java.util.Map;

import org.testng.IClass;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;


/**
 * This class/interface 
 */
public interface IInstanceFactory {
  Object createInstance(
      Class declaringClass,
      Map<Class, IClass> classes,
      XmlTest xmlTest,
      IAnnotationFinder finder);
}
