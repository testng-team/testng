package org.testng.internal;

import static org.testng.internal.ClassHelper.getAvailableMethods;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.DataProviderHolder;
import org.testng.IClass;
import org.testng.IInstanceInfo;
import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.IObjectFactoryAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;

/**
 * This class creates an ITestClass from a test class.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestNGClassFinder extends BaseClassFinder {

  private static final String PREFIX = "[TestNGClassFinder]";
  private final ITestContext m_testContext;
  private final Map<Class<?>, List<Object>> m_instanceMap = Maps.newHashMap();
  private final DataProviderHolder holder;
  private final ITestObjectFactory objectFactory;
  private final IAnnotationFinder annotationFinder;

  private String m_factoryCreationFailedMessage = null;

  public String getFactoryCreationFailedMessage() {
    return m_factoryCreationFailedMessage;
  }

  public TestNGClassFinder(
      ClassInfoMap cim,
      Map<Class<?>, List<Object>> instanceMap,
      IConfiguration configuration,
      ITestContext testContext,
      DataProviderHolder holder) {
    if (instanceMap == null) {
      throw new IllegalArgumentException("instanceMap must not be null");
    }

    m_testContext = testContext;
    this.holder = holder;
    annotationFinder = configuration.getAnnotationFinder();

    // Find all the new classes and their corresponding instances
    Set<Class<?>> allClasses = cim.getClasses();

    objectFactory = createObjectFactory(allClasses, configuration.getObjectFactory());

    for (Class<?> cls : allClasses) {
      processClass(cim, instanceMap, configuration, cls);
    }

    //
    // Add all the instances we found to their respective IClasses
    //
    for (Map.Entry<Class<?>, List<Object>> entry : m_instanceMap.entrySet()) {
      Class<?> clazz = entry.getKey();
      for (Object instance : entry.getValue()) {
        IClass ic = getIClass(clazz);
        if (null != ic) {
          ic.addInstance(instance);
        }
      }
    }
  }

  private void processClass(
      ClassInfoMap cim,
      Map<Class<?>, List<Object>> instanceMap,
      IConfiguration configuration,
      Class<?> cls) {
    if (null == cls) {
      Utils.log(PREFIX, 5, "[WARN] FOUND NULL CLASS");
      return;
    }

    if (isNotTestNGClass(cls, annotationFinder)) { // if not TestNG class
      Utils.log(PREFIX, 3, "SKIPPING CLASS " + cls + " no TestNG annotations found");
      return;
    }
    List<Object> allInstances = instanceMap.get(cls);
    Object thisInstance =
        (allInstances != null && !allInstances.isEmpty()) ? allInstances.get(0) : null;

    // If annotation class and instances are abstract, skip them
    if ((null == thisInstance) && Modifier.isAbstract(cls.getModifiers())) {
      Utils.log("", 5, "[WARN] Found an abstract class with no valid instance attached: " + cls);
      return;
    }

    if ((null == thisInstance) && cls.isAnonymousClass()) {
      Utils.log("", 5, "[WARN] Found an anonymous class with no valid instance attached" + cls);
      return;
    }

    IClass ic =
        findOrCreateIClass(
            m_testContext,
            cls,
            cim.getXmlClass(cls),
            thisInstance,
            annotationFinder,
            objectFactory);
    if (ic == null) {
      return;
    }
    putIClass(cls, ic);

    List<ConstructorOrMethod> factoryMethods =
        ClassHelper.findDeclaredFactoryMethods(cls, annotationFinder);
    for (ConstructorOrMethod factoryMethod : factoryMethods) {
      processMethod(configuration, ic, factoryMethod);
    }
  }

  private void processMethod(
      IConfiguration configuration, IClass ic, ConstructorOrMethod factoryMethod) {
    if (!factoryMethod.getEnabled()) {
      return;
    }
    ClassInfoMap moreClasses = processFactory(ic, factoryMethod);

    if (moreClasses.isEmpty()) {
      return;
    }

    TestNGClassFinder finder =
        new TestNGClassFinder(
            moreClasses, m_instanceMap, configuration, m_testContext, new DataProviderHolder());

    for (IClass ic2 : finder.findTestClasses()) {
      putIClass(ic2.getRealClass(), ic2);
    }
  }

  private static boolean excludeFactory(FactoryMethod fm, ITestContext ctx) {
    return fm.getGroups().length != 0
        && ctx.getCurrentXmlTest().getExcludedGroups().containsAll(Arrays.asList(fm.getGroups()));
  }

  private ClassInfoMap processFactory(IClass ic, ConstructorOrMethod factoryMethod) {
    Object[] theseInstances = ic.getInstances(false);

    Object instance = theseInstances.length != 0 ? theseInstances[0] : null;
    FactoryMethod fm =
        new FactoryMethod(
            factoryMethod, instance, annotationFinder, m_testContext, objectFactory, holder);
    ClassInfoMap moreClasses = new ClassInfoMap();

    if (excludeFactory(fm, m_testContext)) {
      return moreClasses;
    }

    // If the factory returned IInstanceInfo, get the class from it,
    // otherwise, just call getClass() on the returned instances
    int i = 0;
    for (Object o : fm.invoke()) {
      if (o == null) {
        throw new TestNGException(
            "The factory " + fm + " returned a null instance" + "at index " + i);
      }
      Class<?> oneMoreClass;
      Object objToInspect = IParameterInfo.embeddedInstance(o);
      if (IInstanceInfo.class.isAssignableFrom(objToInspect.getClass())) {
        IInstanceInfo<?> ii = (IInstanceInfo) objToInspect;
        addInstance(ii);
        oneMoreClass = ii.getInstanceClass();
      } else {
        addInstance(o);
        oneMoreClass = objToInspect.getClass();
      }
      if (!classExists(oneMoreClass)) {
        moreClasses.addClass(oneMoreClass);
      }
      i++;
    }
    this.m_factoryCreationFailedMessage = fm.getFactoryCreationFailedMessage();
    return moreClasses;
  }

  // TODO use the logic somewhere
  private ITestObjectFactory createObjectFactory(
      Set<Class<?>> allClasses, ITestObjectFactory fallback) {
    for (Class<?> cls : allClasses) {

      try {
        if (cls == null) {
          continue;
        }
        Method[] ms;
        try {
          ms = cls.getMethods();
        } catch (NoClassDefFoundError e) {
          // https://github.com/cbeust/testng/issues/602
          Utils.log(
              PREFIX,
              5,
              "[WARN] Can't link and determine methods of " + cls + "(" + e.getMessage() + ")");
          ms = new Method[0];
        }
        Set<Method> objectMethods =
            Arrays.stream(Object.class.getMethods()).collect(Collectors.toSet());
        ms = Arrays.stream(ms).filter(m -> !objectMethods.contains(m)).toArray(Method[]::new);
        for (Method m : ms) {

          IAnnotation a = annotationFinder.findAnnotation(m, IObjectFactoryAnnotation.class);
          if (a == null) {
            continue;
          }
          if (!ITestObjectFactory.class.isAssignableFrom(m.getReturnType())) {
            throw new TestNGException("Return type of " + m + " is not IObjectFactory");
          }
          try {

            if (m.getParameterTypes().length > 0
                && m.getParameterTypes()[0].equals(ITestContext.class)) {
              return (ITestObjectFactory) m.invoke(fallback.newInstance(cls), m_testContext);
            }
            return (ITestObjectFactory) m.invoke(fallback.newInstance(cls));
          } catch (Exception ex) {
            throw new TestNGException("Error creating object factory: " + cls, ex);
          }
        }
      } catch (NoClassDefFoundError e) {
        Utils.log(
            PREFIX,
            1,
            "Unable to read methods on class "
                + cls.getName()
                + " - unable to resolve class reference "
                + e.getMessage());
        for (XmlClass xmlClass : m_testContext.getCurrentXmlTest().getXmlClasses()) {
          if (xmlClass.loadClasses() && xmlClass.getName().equals(cls.getName())) {
            throw e;
          }
        }
      }
    }
    return fallback;
  }

  private static boolean isNotTestNGClass(Class<?> c, IAnnotationFinder annotationFinder) {
    return (!isTestNGClass(c, annotationFinder));
  }

  /**
   * @return true if this class contains TestNG annotations (either on itself or on a superclass).
   */
  private static boolean isTestNGClass(Class<?> c, IAnnotationFinder annotationFinder) {
    Class<?> cls = c;
    boolean result = false;

    try {
      for (Class<? extends IAnnotation> annotation : AnnotationHelper.getAllAnnotations()) {
        for (cls = c; cls != null; cls = cls.getSuperclass()) {
          // Try on the methods
          for (Method m : getAvailableMethods(cls)) {
            IAnnotation ma = annotationFinder.findAnnotation(cls, m, annotation);
            if (null != ma) {
              // Don't short circuit. Lets run for all methods across all classes.
              // This will in turn ensure that "IgnoreListener" will get called for all the combo.
              result = true;
            }
          }

          // Try on the class
          IAnnotation a = annotationFinder.findAnnotation(cls, annotation);
          if (null != a) {
            // Don't short circuit. Lets run for all methods across all classes.
            // This will in turn ensure that "IgnoreListener" will get called for all the combo.
            result = true;
          }

          // Try on the constructors
          for (Constructor<?> ctor : cls.getConstructors()) {
            IAnnotation ca = annotationFinder.findAnnotation(ctor, annotation);
            if (null != ca) {
              // Don't short circuit. Lets run for all methods across all classes.
              // This will in turn ensure that "IgnoreListener" will get called for all the combo.
              result = true;
            }
          }
        }
      }

      return result;

    } catch (NoClassDefFoundError e) {
      Utils.log(
          PREFIX,
          1,
          "Unable to read methods on class "
              + cls.getName()
              + " - unable to resolve class reference "
              + e.getMessage());
      return false;
    }
  }

  // IInstanceInfo<T> should be replaced by IInstanceInfo<?> but eclipse complains against it.
  // See: https://github.com/cbeust/testng/issues/1070
  private <T> void addInstance(IInstanceInfo<T> ii) {
    addInstance(ii.getInstanceClass(), ii.getInstance());
  }

  private void addInstance(Object o) {
    addInstance(IParameterInfo.embeddedInstance(o).getClass(), o);
  }

  // Class<S> should be replaced by Class<? extends T> but java doesn't fail as expected
  // See: https://github.com/cbeust/testng/issues/1070
  private <T, S extends T> void addInstance(Class<S> clazz, T instance) {
    List<Object> instances = m_instanceMap.computeIfAbsent(clazz, key -> Lists.newArrayList());
    instances.add(instance);
  }
}
