package org.testng.internal;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.testng.*;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.testng.internal.Utils.isStringNotEmpty;

/**
 * Implementation of an IClass.
 */
public class ClassImpl implements IClass {

  private final Class<?> m_class;
  private final IAnnotationFinder m_annotationFinder;
  //    private final Map<Class<?>, IClass> m_classes;
  private final Map<XmlClass, IClass> m_classes;
  private final Object m_instance;
  private final ITestObjectFactory m_objectFactory;
  private final XmlClass m_xmlClass;
  private final ITestContext m_testContext;
  private final boolean m_hasParentModule;
  private Object m_defaultInstance = null;
  private List<Object> m_instances = Lists.newArrayList();
  private int m_instanceCount;
  private long[] m_instanceHashCodes;
  private String m_testName = null;

  /**
   * @deprecated - This constructor is un-used within TestNG and hence stands deprecated as of TestNG v6.13
   */
  @Deprecated
  @SuppressWarnings("unused")
  public ClassImpl(ITestContext context, Class<?> cls, XmlClass xmlClass, Object instance,
//                     Map<Class<?>, IClass> classes, XmlTest xmlTest, IAnnotationFinder annotationFinder,
                   Map<XmlClass, IClass> classes, XmlTest xmlTest, IAnnotationFinder annotationFinder,
                   ITestObjectFactory objectFactory) {
    this(context, cls, xmlClass, instance, classes, annotationFinder, objectFactory);
  }

  public ClassImpl(ITestContext context, Class<?> cls, XmlClass xmlClass, Object instance,
//                     Map<Class<?>, IClass> classes, IAnnotationFinder annotationFinder,
                   Map<XmlClass, IClass> classes, IAnnotationFinder annotationFinder,
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
    m_hasParentModule = isStringNotEmpty(m_testContext.getSuite().getParentModule());
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
  public Class getRealClass() {
    return m_class;
  }

  @Deprecated
  @Override
  public int getInstanceCount() {
    return m_instanceCount;
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

  private Object getDefaultInstance() {
    if (m_defaultInstance == null) {
      if (m_instance != null) {
        m_defaultInstance = m_instance;
      } else {
        Object instance = getInstanceFromGuice();

        if (instance != null) {
          m_defaultInstance = instance;
        } else {
          m_defaultInstance =
//                            ClassHelper.createInstance(m_class, m_classes, m_testContext.getCurrentXmlTest(),
            ClassHelper.createInstance(m_xmlClass, m_classes, m_testContext.getCurrentXmlTest(),
              m_annotationFinder, m_objectFactory);
        }
      }
    }

    return m_defaultInstance;
  }

  /**
   * @return an instance from Guice if @Test(guiceModule) attribute was found, null otherwise
   */
  @SuppressWarnings("unchecked")
  private Object getInstanceFromGuice() {
    Injector injector = m_testContext.getInjector(this);
    if (injector == null) return null;
    return injector.getInstance(m_class);
  }

  public Injector getParentInjector() {
    ISuite suite = m_testContext.getSuite();
    // Reuse the previous parent injector, if any
    Injector injector = suite.getParentInjector();
    if (injector == null) {
      String stageString = suite.getGuiceStage();
      Stage stage;
      if (isStringNotEmpty(stageString)) {
        stage = Stage.valueOf(stageString);
      } else {
        stage = Stage.DEVELOPMENT;
      }
      if (m_hasParentModule) {
        Class<Module> parentModule = (Class<Module>) ClassHelper.forName(suite.getParentModule());
        if (parentModule == null) {
          throw new TestNGException("Cannot load parent Guice module class: " + suite.getParentModule());
        }
        Module module = newModule(parentModule);
        injector = com.google.inject.Guice.createInjector(stage, module);
      } else {
        injector = com.google.inject.Guice.createInjector(stage);
      }
      suite.setParentInjector(injector);
    }
    return injector;
  }

  private Module newModule(Class<Module> module) {
    try {
      Constructor<Module> moduleConstructor = module.getDeclaredConstructor(ITestContext.class);
      return ClassHelper.newInstance(moduleConstructor, m_testContext);
    } catch (NoSuchMethodException e) {
      return ClassHelper.newInstance(module);
    }
  }

  @Override
  public Object[] getInstances(boolean create) {
    Object[] result = {};

    if (m_testContext.getCurrentXmlTest().isJUnit()) {
      if (create) {
//                result = new Object[]{ClassHelper.createInstance(m_class, m_classes,
        result = new Object[]{ClassHelper.createInstance(m_xmlClass, m_classes,
          m_testContext.getCurrentXmlTest(), m_annotationFinder, m_objectFactory)};
      }
    } else {
      Object defaultInstance = getDefaultInstance();
      if (defaultInstance != null) {
        result = new Object[]{defaultInstance};
      }
    }
    if (m_instances.size() > 0) {
      result = m_instances.toArray(new Object[m_instances.size()]);
    }

    m_instanceCount = m_instances.size();
    m_instanceHashCodes = new long[m_instanceCount];
    for (int i = 0; i < m_instanceCount; i++) {
      m_instanceHashCodes[i] = m_instances.get(i).hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
      .add("class", m_class.getName())
      .toString();
  }

  @Override
  public void addInstance(Object instance) {
    m_instances.add(instance);
  }

}
