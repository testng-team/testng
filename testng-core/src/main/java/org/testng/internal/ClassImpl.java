package org.testng.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.testng.IClass;
import org.testng.ITest;
import org.testng.ITestClassInstance;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
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

/** Implementation of an IClass. */
public class ClassImpl implements IClass, IObject {

  private final Class<?> m_class;
  private IObject.IdentifiableObject m_defaultInstance = null;
  private final IAnnotationFinder m_annotationFinder;
  private final List<IObject.IdentifiableObject> identifiableObjects = Lists.newArrayList();
  private final Map<Class<?>, IClass> m_classes;
  private long[] m_instanceHashCodes;
  private final IObject.IdentifiableObject m_instance;
  private final ITestObjectFactory m_objectFactory;
  private String m_testName = null;
  private final XmlClass m_xmlClass;
  private final ITestContext m_testContext;

  public ClassImpl(
      ITestContext context,
      Class<?> cls,
      XmlClass xmlClass,
      IObject.IdentifiableObject instance,
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
    if (IObject.IdentifiableObject.unwrap(instance) instanceof ITest) {
      m_testName = ((ITest) instance.getInstance()).getTestName();
    }
    if (m_testName == null) {
      ITestAnnotation annotation = m_annotationFinder.findAnnotation(cls, ITestAnnotation.class);
      if (annotation != null && !annotation.getTestName().isEmpty()) {
        m_testName = annotation.getTestName();
      }
    }
  }

  @Override
  public String getTestName() {
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
  public long[] getInstanceHashCodes() {
    return m_instanceHashCodes;
  }

  @Override
  public XmlTest getXmlTest() {
    return m_testContext.getCurrentXmlTest();
  }

  @Override
  public XmlClass getXmlClass() {
    return m_xmlClass;
  }

  private IObject.IdentifiableObject getDefaultInstance(boolean create, String errMsgPrefix) {
    if (m_defaultInstance == null) {
      if (m_instance != null) {
        m_defaultInstance = m_instance;
      } else {
        ITestObjectFactory factory = m_objectFactory;
        if (factory instanceof DefaultTestObjectFactory) {
          factory = m_testContext.getSuite().getObjectFactory();
        }
        IObjectDispenser dispenser = Dispenser.newInstance(factory);
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

  @Override
  public Object[] getInstances(boolean create) {
    return getInstances(create, "");
  }

  @Override
  public Object[] getInstances(boolean create, String errorMsgPrefix) {
    return Arrays.stream(getObjects(create, errorMsgPrefix))
        .map(IdentifiableObject::getInstance)
        .toArray(Object[]::new);
  }

  @Override
  public void addObject(IdentifiableObject instance) {
    identifiableObjects.add(instance);
  }

  @Override
  public IdentifiableObject[] getObjects(boolean create, String errorMsgPrefix) {
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
      m_instanceHashCodes[i] = computeHashCode(identifiableObjects.get(i).getInstance());
    }
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("class", m_class.getName()).toString();
  }

  @Override
  public void addInstance(Object instance) {
    addObject(new IdentifiableObject(instance));
  }

  private static int computeHashCode(Object instance) {
    return ITestClassInstance.embeddedInstance(instance).hashCode();
  }

  private DetailedAttributes newDetailedAttributes(boolean create, String errMsgPrefix) {
    DetailedAttributes ea = new DetailedAttributes();
    ea.setXmlTest(m_testContext.getCurrentXmlTest());
    ea.setClasses(m_classes);
    ea.setFinder(m_annotationFinder);
    ea.setDeclaringClass(m_class);
    ea.setErrorMsgPrefix(errMsgPrefix);
    ea.setCreate(create);
    return ea;
  }
}
