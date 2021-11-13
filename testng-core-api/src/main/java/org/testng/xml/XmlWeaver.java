package org.testng.xml;

import org.testng.TestNGException;
import org.testng.internal.ClassHelper;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.objects.InstanceCreator;

/** A Utility class that helps represent a {@link XmlSuite} and {@link XmlTest} as String. */
final class XmlWeaver {
  private static IWeaveXml instance = null;
  private static final boolean testMode = RuntimeBehavior.isTestMode();

  private XmlWeaver() {}

  private static IWeaveXml getInstance() {
    if (testMode) {
      // Donot resort to caching when running Unit tests for TestNG, because we have to check
      // both implementations. If we cache the instance, then its not possible to do that.
      return attemptDefaultImplementationInstantiation();
    }
    return instantiateIfRequired();
  }

  private static IWeaveXml instantiateIfRequired() {
    if (instance != null) {
      return instance;
    }
    instance = attemptDefaultImplementationInstantiation();
    if (instance != null) {
      return instance;
    }
    Class<?> clazz = ClassHelper.forName(getClassName());
    boolean isValid = clazz != null && IWeaveXml.class.isAssignableFrom(clazz);
    if (!isValid) {
      String msg =
          "In order for "
              + getClassName()
              + " to be used by TestNG for generating suite/test xmls, "
              + getClassName()
              + " needs to implement "
              + IWeaveXml.class.getName();
      throw new TestNGException(msg);
    }
    instance = (IWeaveXml) InstanceCreator.newInstance(clazz);
    return instance;
  }

  private static String getClassName() {
    return RuntimeBehavior.getDefaultXmlGenerationImpl();
  }

  /**
   * Helps represent the contents of {@link XmlSuite} as a String.
   *
   * @param suite - The {@link XmlSuite} that needs to be transformed to a String.
   * @return - The String representation
   */
  static String asXml(XmlSuite suite) {
    return getInstance().asXml(suite);
  }

  /**
   * Helps represent the contents of {@link XmlTest} as a String.
   *
   * @param xmlTest - The {@link XmlTest} that needs to be transformed to a String.
   * @param indent - The indentation.
   * @return - The String representation
   */
  static String asXml(XmlTest xmlTest, String indent) {
    return getInstance().asXml(xmlTest, indent);
  }

  private static IWeaveXml attemptDefaultImplementationInstantiation() {
    String clazz = getClassName();
    if (clazz.equals(DefaultXmlWeaver.class.getName())) {
      return new DefaultXmlWeaver();
    }
    if (clazz.equals(CommentDisabledXmlWeaver.class.getName())) {
      return new CommentDisabledXmlWeaver();
    }
    if (testMode) {
      return null;
    }
    Class<?> clazzName = ClassHelper.forName(clazz);
    if (clazzName != null && IWeaveXml.class.isAssignableFrom(clazzName)) {
      return InstanceCreator.newInstance((Class<IWeaveXml>) clazzName);
    }
    throw new IllegalArgumentException(clazz + " does not implement " + IWeaveXml.class.getName());
  }
}
