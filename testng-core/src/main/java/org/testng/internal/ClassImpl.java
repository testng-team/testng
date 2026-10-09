package org.testng.internal;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Objects;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.internal.objects.Dispenser;
import org.testng.internal.objects.IObjectDispenser;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.internal.objects.pojo.DetailedAttributes;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 * Holds what TestNG knows about one test class. This includes the Java class, the {@code <class>}
 * tag and the test instances.
 *
 * <p>When nobody adds a test instance, {@link #getObjects} uses a default instance. That is the
 * instance that the constructor received. Without one, TestNG creates the default instance with the
 * object factory. When that factory is the default one, TestNG uses the factory of the suite
 * instead.
 */
public class ClassImpl implements IClass, IObject {

  private final Class<?> m_class;
  private IObject.@Nullable IdentifiableObject m_defaultInstance = null;
  private final IAnnotationFinder m_annotationFinder;
  private final List<IObject.IdentifiableObject> identifiableObjects = new ArrayList<>();
  private final Map<Class<?>, IClass> m_classes;
  private long[] m_instanceHashCodes = new long[0];
  private final IObject.@Nullable IdentifiableObject m_instance;
  private final ITestObjectFactory m_objectFactory;
  private @Nullable String m_testName = null;
  private final @Nullable XmlClass m_xmlClass;
  private final ITestContext m_testContext;

  /**
   * Creates the data for the test class {@code cls}.
   *
   * <p>The test name comes from {@link ITest#getTestName()} of {@code instance}. Without it, the
   * test name comes from the {@code testName} of a {@code @Test} annotation on the class.
   *
   * @param context the context of the {@code <test>}.
   * @param cls the Java class.
   * @param xmlClass the {@code <class>} tag of {@code cls}, or {@code null} when no tag names it.
   * @param instance the test instance, or {@code null} to let TestNG create one when it needs it.
   * @param classes the test classes that the class finder keeps. TestNG passes them on when it
   *     creates the instance.
   * @param annotationFinder the finder that reads the annotations.
   * @param objectFactory the factory that creates the test instance.
   */
  public ClassImpl(
      ITestContext context,
      Class<?> cls,
      @Nullable XmlClass xmlClass,
      IObject.@Nullable IdentifiableObject instance,
      Map<Class<?>, IClass> classes,
      IAnnotationFinder annotationFinder,
      ITestObjectFactory objectFactory) {
    m_testContext = context;
    m_class = cls;
    m_classes = classes;
    m_xmlClass = xmlClass;
    m_annotationFinder = annotationFinder;
    m_instance = instance;
    m_objectFactory = objectFactory;
    Object unwrapped = IObject.IdentifiableObject.unwrap(instance);
    if (unwrapped instanceof ITest) {
      m_testName = ((ITest) unwrapped).getTestName();
    }
    if (m_testName == null) {
      ITestAnnotation annotation = m_annotationFinder.findAnnotation(cls, ITestAnnotation.class);
      if (annotation != null && !annotation.getTestName().isEmpty()) {
        m_testName = annotation.getTestName();
      }
    }
  }

  @Override
  public @Nullable String getTestName() {
    return m_testName;
  }

  @Override
  public String getName() {
    return m_class.getName();
  }

  @Override
  public Class<?> getRealClass() {
    return m_class;
  }

  @Override
  public long[] getObjectHashCodes() {
    return m_instanceHashCodes;
  }

  @Deprecated
  @Override
  public long[] getInstanceHashCodes() {
    return getObjectHashCodes();
  }

  @Override
  public XmlTest getXmlTest() {
    return m_testContext.getCurrentXmlTest();
  }

  @Override
  public @Nullable XmlClass getXmlClass() {
    return m_xmlClass;
  }

  private IObject.@Nullable IdentifiableObject getDefaultInstance(
      boolean create, @Nullable String errMsgPrefix) {
    if (m_defaultInstance == null) {
      if (m_instance != null) {
        m_defaultInstance = m_instance;
      } else {
        ITestObjectFactory factory = m_objectFactory;
        if (factory instanceof DefaultTestObjectFactory) {
          factory = m_testContext.getSuite().getObjectFactory();
        }
        IObjectDispenser dispenser =
            Dispenser.newInstance(
                requireNonNull(factory, "a running suite carries an object factory"));
        BasicAttributes basic = new BasicAttributes(this, null);
        DetailedAttributes detailed = newDetailedAttributes(create, errMsgPrefix);
        CreationAttributes attributes = new CreationAttributes(m_testContext, basic, detailed);
        Object raw = dispenser.dispense(attributes);
        if (raw != null) {
          m_defaultInstance = new IObject.IdentifiableObject(raw);
        }
      }
    }
    return m_defaultInstance;
  }

  @Deprecated
  @Override
  public Object[] getInstances(boolean create) {
    return getInstances(create, "");
  }

  @Deprecated
  @Override
  public Object[] getInstances(boolean create, @Nullable String errorMsgPrefix) {
    return Arrays.stream(getObjects(create, errorMsgPrefix))
        .map(IdentifiableObject::getInstance)
        .toArray(Object[]::new);
  }

  @Override
  public void addObject(IdentifiableObject instance) {
    identifiableObjects.add(instance);
  }

  @Override
  public IdentifiableObject[] getObjects(boolean create, @Nullable String errorMsgPrefix) {
    IdentifiableObject[] result = {};

    if (!identifiableObjects.isEmpty()) {
      result = identifiableObjects.toArray(new IdentifiableObject[0]);
    } else {
      IdentifiableObject defaultInstance = getDefaultInstance(create, errorMsgPrefix);
      if (defaultInstance != null) {
        result = new IdentifiableObject[] {defaultInstance};
      }
    }

    int m_instanceCount = identifiableObjects.size();
    m_instanceHashCodes = new long[m_instanceCount];
    for (int i = 0; i < m_instanceCount; i++) {
      m_instanceHashCodes[i] = computeHashCode(identifiableObjects.get(i));
    }
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("class", m_class.getName()).toString();
  }

  @Deprecated
  @Override
  public void addInstance(Object instance) {
    addObject(new IdentifiableObject(instance));
  }

  private static int computeHashCode(IdentifiableObject identifiable) {
    Object instance = identifiable.getInstance();
    if (instance instanceof IParameterInfo
        && !((IParameterInfo) instance).isInstanceInstantiated()) {
      // Do not create a lazy @Factory instance only to get a hash code. Use the hash code of its
      // instance id instead, which is unique and does not change.
      return identifiable.getInstanceId().hashCode();
    }
    return requireNonNull(
            IParameterInfo.embeddedInstance(instance), "the factory instance is not available")
        .hashCode();
  }

  private DetailedAttributes newDetailedAttributes(boolean create, @Nullable String errMsgPrefix) {
    return new DetailedAttributes(
        m_class,
        m_classes,
        m_testContext.getCurrentXmlTest(),
        m_annotationFinder,
        create,
        errMsgPrefix);
  }
}
