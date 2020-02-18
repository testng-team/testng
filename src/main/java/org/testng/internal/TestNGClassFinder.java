package org.testng.internal;

import org.testng.*;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.IObjectFactoryAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.testng.internal.ClassHelper.getAvailableMethods;

/**
 * This class creates an ITestClass from a test class.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestNGClassFinder extends BaseClassFinder {

  private static final String PREFIX = "[TestNGClassFinder]";
  private final ITestContext m_testContext;
  //    private final Map<Class<?>, List<Object>> m_instanceMap = Maps.newHashMap();
  private final Map<XmlClass, List<Object>> m_instanceMap = Maps.newHashMap();
  private final Map<Class<? extends IDataProviderListener>, IDataProviderListener> m_dataProviderListeners;
  private final ITestObjectFactory objectFactory;
  private final IAnnotationFinder annotationFinder;

  /**
   * @deprecated - This constructor is un-used within TestNG and hence stands deprecated as of TestNG v6.13
   */
  @Deprecated
  @SuppressWarnings("unused")
  public TestNGClassFinder(ClassInfoMap cim,
                           XmlTest xmlTest,
                           IConfiguration configuration,
                           ITestContext testContext) {
//        this(cim, Maps.<Class<?>, List<Object>>newHashMap(), configuration, testContext,
    this(cim, Maps.<XmlClass, List<Object>>newHashMap(), configuration, testContext,
      Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap());
  }

  /**
   * @deprecated - This constructor is un-used within TestNG and hence stands deprecated as of TestNG v6.13
   */
  @Deprecated
  @SuppressWarnings("unused")
  public TestNGClassFinder(ClassInfoMap cim,
                           XmlTest xmlTest,
                           IConfiguration configuration,
                           ITestContext testContext,
                           Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
//        this(cim, Maps.<Class<?>, List<Object>>newHashMap(), configuration, testContext, dataProviderListeners);
    this(cim, Maps.<XmlClass, List<Object>>newHashMap(), configuration, testContext, dataProviderListeners);
  }

  /**
   * @deprecated - This constructor is un-used within TestNG and hence stands deprecated as of TestNG v6.13
   */
  @Deprecated
  @SuppressWarnings("unused")
  public TestNGClassFinder(ClassInfoMap cim,
//                             Map<Class<?>, List<Object>> instanceMap,
                           Map<XmlClass, List<Object>> instanceMap,
                           XmlTest xmlTest,
                           IConfiguration configuration,
                           ITestContext testContext) {
    this(cim, instanceMap, configuration, testContext,
      Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap());
  }

  /**
   * @deprecated - This constructor is un-used within TestNG and hence stands deprecated as of TestNG v6.13
   */
  @Deprecated
  @SuppressWarnings("unused")
  public TestNGClassFinder(ClassInfoMap cim,
//                             Map<Class<?>, List<Object>> instanceMap,
                           Map<XmlClass, List<Object>> instanceMap,
                           XmlTest xmlTest,
                           IConfiguration configuration,
                           ITestContext testContext,
                           Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
    this(cim, instanceMap, configuration, testContext, dataProviderListeners);
  }

  public TestNGClassFinder(ClassInfoMap cim,
//                             Map<Class<?>, List<Object>> instanceMap,
                           Map<XmlClass, List<Object>> instanceMap,
                           IConfiguration configuration,
                           ITestContext testContext,
                           Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
    if (instanceMap == null) {
      throw new IllegalArgumentException("instanceMap must not be null");
    }

    m_testContext = testContext;
    m_dataProviderListeners = dataProviderListeners;
    annotationFinder = configuration.getAnnotationFinder();

    // Find all the new classes and their corresponding instances
//        Set<Class<?>> allClasses = cim.getClasses();
    Set<XmlClass> allClasses = cim.getXmlClasses();

    //very first pass is to find ObjectFactory, can't create anything else until then
    if (configuration.getObjectFactory() == null) {
      objectFactory = createObjectFactory(allClasses);
    } else {
      objectFactory = configuration.getObjectFactory();
    }

//        for (Class<?> cls : allClasses) {
    for (XmlClass cls : allClasses) {
      processClass(cim, instanceMap, configuration, cls);
    }

    //
    // Add all the instances we found to their respective IClasses
    //
//        for (Map.Entry<Class<?>, List<Object>> entry : m_instanceMap.entrySet()) {
//            Class<?> clazz = entry.getKey();
//            for (Object instance : entry.getValue()) {
//                IClass ic = getIClass(clazz);
//                if (null != ic) {
//                    ic.addInstance(instance);
//                }
//            }
//        }

    for (Map.Entry<XmlClass, List<Object>> entry : m_instanceMap.entrySet()) {
      XmlClass xmlClass = entry.getKey();
      for (Object instance : entry.getValue()) {
        IClass ic = getIClass(xmlClass);
        if (null != ic) {
          ic.addInstance(instance);
        }
      }
    }

  }

  private static boolean excludeFactory(FactoryMethod fm, ITestContext ctx) {
    return fm.getGroups().length != 0
      && ctx.getCurrentXmlTest().getExcludedGroups().containsAll(Arrays.asList(fm.getGroups()));
  }

  private static boolean isNotTestNGClass(Class<?> c, IAnnotationFinder annotationFinder) {
    return (!isTestNGClass(c, annotationFinder));
  }

  /**
   * @return true if this class contains TestNG annotations (either on itself
   * or on a superclass).
   */
  private static boolean isTestNGClass(Class<?> c, IAnnotationFinder annotationFinder) {
    Class<?> cls = c;

    try {
      for (Class<? extends IAnnotation> annotation : AnnotationHelper.getAllAnnotations()) {
        for (cls = c; cls != null; cls = cls.getSuperclass()) {
          // Try on the methods
          for (Method m : getAvailableMethods(cls)) {
            IAnnotation ma = annotationFinder.findAnnotation(m, annotation);
            if (null != ma) {
              return true;
            }
          }

          // Try on the class
          IAnnotation a = annotationFinder.findAnnotation(cls, annotation);
          if (null != a) {
            return true;
          }

          // Try on the constructors
          for (Constructor ctor : cls.getConstructors()) {
            IAnnotation ca = annotationFinder.findAnnotation(ctor, annotation);
            if (null != ca) {
              return true;
            }
          }
        }
      }

      return false;

    } catch (NoClassDefFoundError e) {
      Utils.log(PREFIX, 1,
        "Unable to read methods on class " + cls.getName()
          + " - unable to resolve class reference " + e.getMessage());
      return false;
    }
  }

  //    private void processClass(ClassInfoMap cim, Map<Class<?>, List<Object>> instanceMap, IConfiguration configuration, Class<?> cls) {
  private void processClass(ClassInfoMap cim, Map<XmlClass, List<Object>> instanceMap, IConfiguration configuration, XmlClass xmlClass) {
    if (null == xmlClass) {
      Utils.log(PREFIX, 5, "[WARN] FOUND NULL CLASS");
      return;
    }

    Class<?> cls = xmlClass.getSupportClass();

    if (isNotTestNGClass(cls, annotationFinder)) { // if not TestNG class
      Utils.log(PREFIX, 3, "SKIPPING CLASS " + cls + " no TestNG annotations found");
      return;
    }
//        List<Object> allInstances = instanceMap.get(cls);
    List<Object> allInstances = instanceMap.get(xmlClass);
    Object thisInstance = (allInstances != null && !allInstances.isEmpty()) ? allInstances.get(0) : null;

    // If annotation class and instances are abstract, skip them
    if ((null == thisInstance) && Modifier.isAbstract(cls.getModifiers())) {
      Utils.log("", 5, "[WARN] Found an abstract class with no valid instance attached: " + cls);
      return;
    }

    if ((null == thisInstance) && cls.isAnonymousClass()) {
      Utils.log("", 5, "[WARN] Found an anonymous class with no valid instance attached" + cls);
      return;
    }

//        IClass ic = findOrCreateIClass(m_testContext, cls, cim.getXmlClass(cls), thisInstance, annotationFinder, objectFactory);
    IClass ic = findOrCreateIClass(m_testContext, cls, xmlClass, thisInstance, annotationFinder, objectFactory);
    if (ic == null) {
      return;
    }
//        putIClass(cls, ic);
    putIClass(xmlClass, ic);

    List<ConstructorOrMethod> factoryMethods = ClassHelper.findDeclaredFactoryMethods(cls, annotationFinder);
    for (ConstructorOrMethod factoryMethod : factoryMethods) {
      processMethod(configuration, ic, factoryMethod);
    }
  }

  private void processMethod(IConfiguration configuration, IClass ic, ConstructorOrMethod factoryMethod) {
    if (!factoryMethod.getEnabled()) {
      return;
    }
    ClassInfoMap moreClasses = processFactory(ic, factoryMethod);

    if (moreClasses.isEmpty()) {
      return;
    }

    TestNGClassFinder finder =
      new TestNGClassFinder(moreClasses,
        m_instanceMap,
        configuration,
        m_testContext,
        Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap());

    for (IClass ic2 : finder.findTestClasses()) {
//            putIClass(ic2.getRealClass(), ic2);
      putIClass(ic2.getXmlClass(), ic2);
    }
  }

  private ClassInfoMap processFactory(IClass ic, ConstructorOrMethod factoryMethod) {
    Object[] theseInstances = ic.getInstances(false);
    if (theseInstances.length == 0) {
      theseInstances = ic.getInstances(true);
    }

    Object instance = theseInstances.length != 0 ? theseInstances[0] : null;
    FactoryMethod fm = new FactoryMethod(
      ic.getXmlClass(),
      factoryMethod,
      instance,
      annotationFinder,
      m_testContext, objectFactory, m_dataProviderListeners);
    ClassInfoMap moreClasses = new ClassInfoMap();

    if (excludeFactory(fm, m_testContext)) {
      return moreClasses;
    }

    // If the factory returned IInstanceInfo, get the class from it,
    // otherwise, just call getClass() on the returned instances
    int i = 0;
    for (Object o : fm.invoke()) {
      if (o == null) {
        throw new TestNGException("The factory " + fm + " returned a null instance" + "at index " + i);
      }
      Class<?> oneMoreClass;
      if (IInstanceInfo.class.isAssignableFrom(o.getClass())) {
        IInstanceInfo<?> ii = (IInstanceInfo) o;
        addInstance(ii, ic.getXmlClass());
        oneMoreClass = ii.getInstanceClass();
      } else {
        addInstance(o, ic.getXmlClass());
        oneMoreClass = o.getClass();
      }
//            if (!classExists(oneMoreClass)) {
//                moreClasses.addClass(oneMoreClass);
//            }
      XmlClass cloneXmlClass = (XmlClass) ic.getXmlClass().clone();
      cloneXmlClass.setClass(oneMoreClass);
      moreClasses.addXmlClass(cloneXmlClass);
      i++;
    }
    return moreClasses;
  }

  //    private ITestObjectFactory createObjectFactory(Set<Class<?>> allClasses) {
  private ITestObjectFactory createObjectFactory(Set<XmlClass> allClasses) {
    ITestObjectFactory objectFactory;
    objectFactory = new ObjectFactoryImpl();
//        for (Class<?> cls : allClasses) {
    for (XmlClass xmlClass : allClasses) {
      Class cls = xmlClass.getSupportClass();
      try {
        if (cls == null) {
          continue;
        }
        Method[] ms;
        try {
          ms = cls.getMethods();
        } catch (NoClassDefFoundError e) {
          // https://github.com/cbeust/testng/issues/602
          Utils.log(PREFIX, 5, "[WARN] Can't link and determine methods of " + cls + "(" + e.getMessage() + ")");
          ms = new Method[0];
        }
        for (Method m : ms) {
          IAnnotation a = annotationFinder.findAnnotation(m, IObjectFactoryAnnotation.class);
          if (a == null) {
            continue;
          }
          if (!ITestObjectFactory.class.isAssignableFrom(m.getReturnType())) {
            throw new TestNGException("Return type of " + m + " is not IObjectFactory");
          }
          try {
            Object instance = cls.newInstance();
            if (m.getParameterTypes().length > 0 && m.getParameterTypes()[0].equals(ITestContext.class)) {
              objectFactory = (ITestObjectFactory) m.invoke(instance, m_testContext);
            } else {
              objectFactory = (ITestObjectFactory) m.invoke(instance);
            }
            return objectFactory;
          } catch (Exception ex) {
            throw new TestNGException("Error creating object factory: " + cls, ex);
          }
        }
      } catch (NoClassDefFoundError e) {
        Utils.log(PREFIX, 1, "Unable to read methods on class " + cls.getName()
          + " - unable to resolve class reference " + e.getMessage());
        for (XmlClass c : m_testContext.getCurrentXmlTest().getXmlClasses()) {
          if (c.loadClasses() && c.getName().equals(cls.getName())) {
            throw e;
          }
        }

      }
    }
    return objectFactory;
  }

  // IInstanceInfo<T> should be replaced by IInstanceInfo<?> but eclipse complains against it.
  // See: https://github.com/cbeust/testng/issues/1070
//    private <T> void addInstance(IInstanceInfo<T> ii) {
//        addInstance(ii.getInstanceClass(), ii.getInstance());
//    }

  private <T> void addInstance(IInstanceInfo<T> ii, XmlClass xmlClass) {
    addInstance(ii.getInstanceClass(), ii.getInstance(), xmlClass);
  }

//    private void addInstance(Object o) {
//        addInstance(o.getClass(), o);
//    }

  private void addInstance(Object o, XmlClass xmlClass) {
    addInstance(o.getClass(), o, xmlClass);
  }

  // Class<S> should be replaced by Class<? extends T> but java doesn't fail as expected
  // See: https://github.com/cbeust/testng/issues/1070
//    private <T, S extends T> void addInstance(Class<S> clazz, T instance) {
//        List<Object> instances = m_instanceMap.get(clazz);
//
//        if (instances == null) {
//            instances = Lists.newArrayList();
//            m_instanceMap.put(clazz, instances);
//        }
//
//        instances.add(instance);
//    }

  private <T, S extends T> void addInstance(Class<S> clazz, T instance, XmlClass xmlClass) {
    List<Object> instances = m_instanceMap.computeIfAbsent(xmlClass, k -> Lists.newArrayList());

    instances.add(instance);
  }
}
