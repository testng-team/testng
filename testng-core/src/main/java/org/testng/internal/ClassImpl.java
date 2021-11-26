package org.testng.internal;

import java.util.List;
import java.util.Map;
import org.testng.IClass;
import org.testng.ITest;
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
public class ClassImpl implements IClass {

  private final Class<?> m_class;
  private Object m_defaultInstance = null;
  private final IAnnotationFinder m_annotationFinder;
  private final List<Object> m_instances = Lists.newArrayList();
  private final Map<Class<?>, IClass> m_classes;
  private long[] m_instanceHashCodes;
  private final Object m_instance;
  private final ITestObjectFactory m_objectFactory;
  private String m_testName = null;
  private final XmlClass m_xmlClass;
  private final ITestContext m_testContext;

  public ClassImpl(
      ITestContext context,
      Class<?> cls,
      XmlClass xmlClass,
      Object instance,
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
    if (instance instanceof ITest) {
      m_testName = ((ITest) instance).getTestName();
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

  private Object getDefaultInstance(boolean create, String errMsgPrefix) {
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
        m_defaultInstance = dispenser.dispense(attributes);
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
    Object[] result = {};

    if (m_testContext.getCurrentXmlTest().isJUnit()) {
      if (create) {
        DetailedAttributes ea = newDetailedAttributes(create, errorMsgPrefix);
        CreationAttributes attributes = new CreationAttributes(m_testContext, null, ea);
        result =
            new Object[] {
              Dispenser.newInstance(m_testContext.getSuite().getObjectFactory())
                  .dispense(attributes)
            };
      }
    }
    if (m_instances.size() > 0) {
      result = m_instances.toArray(new Object[0]);
    } else {
      Object defaultInstance = getDefaultInstance(create, errorMsgPrefix);
      if (defaultInstance != null) {
        result = new Object[] {defaultInstance};
      }
    }

    int m_instanceCount = m_instances.size();
    m_instanceHashCodes = new long[m_instanceCount];
    for (int i = 0; i < m_instanceCount; i++) {
      m_instanceHashCodes[i] = computeHashCode(m_instances.get(i));
    }
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("class", m_class.getName()).toString();
  }

  @Override
  public void addInstance(Object instance) {
    m_instances.add(instance);
  }

  private static int computeHashCode(Object instance) {
    return IParameterInfo.embeddedInstance(instance).hashCode();
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
