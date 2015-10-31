package org.testng.internal;

import static org.testng.internal.Utils.isStringNotEmpty;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.testng.IClass;
import org.testng.IModuleFactory;
import org.testng.ISuite;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.Guice;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Implementation of an IClass.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ClassImpl implements IClass {
  private static final long serialVersionUID = 1118178273317520344L;
  transient private Class m_class = null;
  transient private Object m_defaultInstance = null;
  private XmlTest m_xmlTest = null;
  transient private IAnnotationFinder m_annotationFinder = null;
  transient private List<Object> m_instances = Lists.newArrayList();
  transient private Map<Class, IClass> m_classes = null;
  private int m_instanceCount;
  private long[] m_instanceHashCodes;
  private transient Object m_instance;
  private ITestObjectFactory m_objectFactory;
  private String m_testName = null;
  private XmlClass m_xmlClass;
  private ITestContext m_testContext;
  private final boolean m_hasParentModule;

  public ClassImpl(ITestContext context, Class cls, XmlClass xmlClass, Object instance,
      Map<Class, IClass> classes, XmlTest xmlTest, IAnnotationFinder annotationFinder,
      ITestObjectFactory objectFactory) {
    m_testContext = context;
    m_class = cls;
    m_classes = classes;
    m_xmlClass = xmlClass;
    m_xmlTest = xmlTest;
    m_annotationFinder = annotationFinder;
    m_instance = instance;
    m_objectFactory = objectFactory;
    if (instance instanceof ITest) {
      m_testName = ((ITest) instance).getTestName();
    }
    if (m_testName == null) {
      ITestAnnotation annotation = m_annotationFinder.findAnnotation(cls, ITestAnnotation.class);
      if (annotation != null) {
        m_testName = annotation.getTestName();
      }
    }
    m_hasParentModule = isStringNotEmpty(m_testContext.getSuite().getParentModule());
  }

  private static void ppp(String s) {
    System.out.println("[ClassImpl] " + s);
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
    return m_xmlTest;
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
              ClassHelper.createInstance(m_class, m_classes, m_xmlTest,
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
          throw new TestNGException("Cannot load parent Guice module class: " + parentModule);
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

    if (m_xmlTest.isJUnit()) {
      if (create) {
        result = new Object[] { ClassHelper.createInstance(m_class, m_classes,
            m_xmlTest, m_annotationFinder, m_objectFactory) };
      }
    } else {
      result = new Object[] { getDefaultInstance() };
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
