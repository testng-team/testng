package org.testng.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.ITestClassFinder;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;

/**
 * Keeps the test classes that a class finder finds, one {@link IClass} for each Java class.
 *
 * <p>A subclass adds the test classes with {@link #putIClass} or {@link #findOrCreateIClass}.
 * {@link #findTestClasses()} returns them in the order in which they were added.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public abstract class BaseClassFinder implements ITestClassFinder {
  private final Map<Class<?>, IClass> m_classes = new LinkedHashMap<>();

  @Override
  public @Nullable IClass getIClass(Class<?> cls) {
    return m_classes.get(cls);
  }

  /**
   * Adds {@code iClass} for {@code cls}, unless this finder already has a test class for {@code
   * cls}.
   *
   * @param cls the Java class.
   * @param iClass the test class to add.
   */
  protected void putIClass(Class<?> cls, IClass iClass) {
    if (!m_classes.containsKey(cls)) {
      m_classes.put(cls, iClass);
    }
  }

  /**
   * Returns the test class for {@code cls}. When this finder has none yet, this method creates a
   * {@link ClassImpl} and keeps it.
   *
   * @param context the context of the {@code <test>}.
   * @param cls the Java class.
   * @param xmlClass the {@code <class>} tag of {@code cls}, or {@code null} when no tag names it.
   * @param instance the test instance, or {@code null} to let TestNG create one when it needs it.
   * @param annotationFinder the finder that reads the annotations.
   * @param objectFactory the factory that creates the test instances.
   * @return the test class that this finder keeps for {@code cls}.
   */
  protected IClass findOrCreateIClass(
      ITestContext context,
      Class<?> cls,
      @Nullable XmlClass xmlClass,
      IObject.@Nullable IdentifiableObject instance,
      IAnnotationFinder annotationFinder,
      ITestObjectFactory objectFactory) {

    return m_classes.computeIfAbsent(
        cls,
        key ->
            new ClassImpl(
                context, key, xmlClass, instance, m_classes, annotationFinder, objectFactory));
  }

  /**
   * Tells if this finder has a test class for {@code cls}.
   *
   * @param cls the Java class.
   * @return {@code true} when this finder has a test class for {@code cls}.
   */
  protected boolean classExists(Class<?> cls) {
    return m_classes.containsKey(cls);
  }

  @Override
  public IClass[] findTestClasses() {
    return m_classes.values().toArray(new IClass[0]);
  }
}
