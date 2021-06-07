package org.testng.internal.objects.pojo;

import java.util.Map;
import org.testng.IClass;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/** Represents the elaborate set of attributes required for object creation. */
public class DetailedAttributes {

  private Class<?> declaringClass;
  private Map<Class<?>, IClass> classes;
  private XmlTest xmlTest;
  private IAnnotationFinder finder;
  private boolean create;
  private String errorMsgPrefix;

  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  public void setDeclaringClass(Class<?> declaringClass) {
    this.declaringClass = declaringClass;
  }

  public Map<Class<?>, IClass> getClasses() {
    return classes;
  }

  public void setClasses(Map<Class<?>, IClass> classes) {
    this.classes = classes;
  }

  public XmlTest getXmlTest() {
    return xmlTest;
  }

  public void setXmlTest(XmlTest xmlTest) {
    this.xmlTest = xmlTest;
  }

  public IAnnotationFinder getFinder() {
    return finder;
  }

  public void setFinder(IAnnotationFinder finder) {
    this.finder = finder;
  }

  public boolean isCreate() {
    return create;
  }

  public void setCreate(boolean create) {
    this.create = create;
  }

  public String getErrorMsgPrefix() {
    return errorMsgPrefix;
  }

  public void setErrorMsgPrefix(String errorMsgPrefix) {
    this.errorMsgPrefix = errorMsgPrefix;
  }
}
