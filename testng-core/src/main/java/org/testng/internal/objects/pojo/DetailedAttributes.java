package org.testng.internal.objects.pojo;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/** Represents the elaborate set of attributes required for object creation. */
public class DetailedAttributes {

  private final Class<?> declaringClass;
  private final Map<Class<?>, IClass> classes;
  private final XmlTest xmlTest;
  private final IAnnotationFinder finder;
  private final boolean create;
  private final @Nullable String errorMsgPrefix;

  public DetailedAttributes(
      Class<?> declaringClass,
      Map<Class<?>, IClass> classes,
      XmlTest xmlTest,
      IAnnotationFinder finder,
      boolean create,
      @Nullable String errorMsgPrefix) {
    this.declaringClass = declaringClass;
    this.classes = classes;
    this.xmlTest = xmlTest;
    this.finder = finder;
    this.create = create;
    this.errorMsgPrefix = errorMsgPrefix;
  }

  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  public Map<Class<?>, IClass> getClasses() {
    return classes;
  }

  public XmlTest getXmlTest() {
    return xmlTest;
  }

  public IAnnotationFinder getFinder() {
    return finder;
  }

  public boolean isCreate() {
    return create;
  }

  public @Nullable String getErrorMsgPrefix() {
    return errorMsgPrefix;
  }
}
