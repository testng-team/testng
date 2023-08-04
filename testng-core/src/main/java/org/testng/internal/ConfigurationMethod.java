package org.testng.internal;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.ConfigurationAnnotation;
import org.testng.internal.annotations.IAfterClass;
import org.testng.internal.annotations.IAfterGroups;
import org.testng.internal.annotations.IAfterMethod;
import org.testng.internal.annotations.IAfterSuite;
import org.testng.internal.annotations.IAfterTest;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IBaseBeforeAfterMethod;
import org.testng.internal.annotations.IBeforeClass;
import org.testng.internal.annotations.IBeforeGroups;
import org.testng.internal.annotations.IBeforeMethod;
import org.testng.internal.annotations.IBeforeSuite;
import org.testng.internal.annotations.IBeforeTest;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlTest;

public class ConfigurationMethod extends BaseTestMethod {

  private final boolean m_isBeforeSuiteConfiguration;
  private final boolean m_isAfterSuiteConfiguration;

  private final boolean m_isBeforeTestConfiguration;
  private final boolean m_isAfterTestConfiguration;

  private final boolean m_isBeforeClassConfiguration;
  private final boolean m_isAfterClassConfiguration;

  private final boolean m_isBeforeMethodConfiguration;
  private final boolean m_isAfterMethodConfiguration;

  private boolean m_isBeforeGroupsConfiguration;
  private boolean m_isAfterGroupsConfiguration;

  private final boolean m_isIgnoreFailure;
  private boolean m_inheritGroupsFromTestClass = false;
  private final IParameterInfo factoryMethodInfo;

  private ConfigurationMethod(
      ITestObjectFactory objectFactory,
      ConstructorOrMethod com,
      IAnnotationFinder annotationFinder,
      boolean isBeforeSuite,
      boolean isAfterSuite,
      boolean isBeforeTest,
      boolean isAfterTest,
      boolean isBeforeClass,
      boolean isAfterClass,
      boolean isBeforeMethod,
      boolean isAfterMethod,
      boolean isIgnoreFailure,
      String[] beforeGroups,
      String[] afterGroups,
      boolean initialize,
      Object instance) {
    super(
        objectFactory,
        com.getName(),
        com,
        annotationFinder,
        IParameterInfo.embeddedInstance(instance));
    if (initialize) {
      init();
    }

    this.factoryMethodInfo =
        (instance instanceof IParameterInfo) ? (IParameterInfo) instance : null;

    m_isBeforeSuiteConfiguration = isBeforeSuite;
    m_isAfterSuiteConfiguration = isAfterSuite;

    m_isBeforeTestConfiguration = isBeforeTest;
    m_isAfterTestConfiguration = isAfterTest;

    m_isBeforeClassConfiguration = isBeforeClass;
    m_isAfterClassConfiguration = isAfterClass;

    m_isBeforeMethodConfiguration = isBeforeMethod;
    m_isAfterMethodConfiguration = isAfterMethod;

    m_isIgnoreFailure = isIgnoreFailure;

    m_beforeGroups = beforeGroups;
    m_afterGroups = afterGroups;
  }

  @Override
  public IParameterInfo getFactoryMethodParamsInfo() {
    if (this.factoryMethodInfo != null) {
      return this.factoryMethodInfo;
    }
    return super.getFactoryMethodParamsInfo();
  }

  public ConfigurationMethod(
      ITestObjectFactory objectFactory,
      ConstructorOrMethod com,
      IAnnotationFinder annotationFinder,
      boolean isBeforeSuite,
      boolean isAfterSuite,
      boolean isBeforeTest,
      boolean isAfterTest,
      boolean isBeforeClass,
      boolean isAfterClass,
      boolean isBeforeMethod,
      boolean isAfterMethod,
      boolean isIgnoreFailure,
      String[] beforeGroups,
      String[] afterGroups,
      XmlTest xmlTest,
      Object instance) {
    this(
        objectFactory,
        com,
        annotationFinder,
        isBeforeSuite,
        isAfterSuite,
        isBeforeTest,
        isAfterTest,
        isBeforeClass,
        isAfterClass,
        isBeforeMethod,
        isAfterMethod,
        isIgnoreFailure,
        beforeGroups,
        afterGroups,
        true,
        instance);
    setXmlTest(xmlTest);
  }

  private static ITestNGMethod[] createMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder finder,
      boolean isBeforeSuite,
      boolean isAfterSuite,
      boolean isBeforeTest,
      boolean isAfterTest,
      boolean isBeforeClass,
      boolean isAfterClass,
      boolean isBeforeMethod,
      boolean isAfterMethod,
      XmlTest xmlTest,
      Object instance) {
    List<ITestNGMethod> result = Lists.newArrayList();
    for (ITestNGMethod method : methods) {
      if (Modifier.isStatic(method.getConstructorOrMethod().getMethod().getModifiers())) {
        String msg =
            "Detected a static method ["
                + method.getQualifiedName()
                + "()]. Static configuration methods can cause "
                + "unexpected behavior.";
        Logger.getLogger(Configuration.class).warn(msg);
      }

      result.add(
          new ConfigurationMethod(
              objectFactory,
              method.getConstructorOrMethod(),
              finder,
              isBeforeSuite,
              isAfterSuite,
              isBeforeTest,
              isAfterTest,
              isBeforeClass,
              isAfterClass,
              isBeforeMethod,
              isAfterMethod,
              method.isIgnoreFailure(),
              new String[0],
              new String[0],
              xmlTest,
              instance));
    }

    return result.toArray(new ITestNGMethod[0]);
  }

  public static ITestNGMethod[] createSuiteConfigurationMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder,
      boolean isBefore,
      Object instance) {

    return createMethods(
        objectFactory,
        methods,
        annotationFinder,
        isBefore,
        !isBefore,
        false,
        false,
        false,
        false,
        false,
        false,
        null,
        instance);
  }

  public static ITestNGMethod[] createTestConfigurationMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder,
      boolean isBefore,
      XmlTest xmlTest,
      Object instance) {
    return createMethods(
        objectFactory,
        methods,
        annotationFinder,
        false,
        false,
        isBefore,
        !isBefore,
        false,
        false,
        false,
        false,
        xmlTest,
        instance);
  }

  public static ITestNGMethod[] createClassConfigurationMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder,
      boolean isBefore,
      XmlTest xmlTest,
      Object instance) {
    return createMethods(
        objectFactory,
        methods,
        annotationFinder,
        false,
        false,
        false,
        false,
        isBefore,
        !isBefore,
        false,
        false,
        xmlTest,
        instance);
  }

  public static ITestNGMethod[] createBeforeConfigurationMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder,
      boolean isBefore,
      Object instance) {
    ITestNGMethod[] result = new ITestNGMethod[methods.length];
    for (int i = 0; i < methods.length; i++) {
      result[i] =
          new ConfigurationMethod(
              objectFactory,
              methods[i].getConstructorOrMethod(),
              annotationFinder,
              false,
              false,
              false,
              false,
              false,
              false,
              false,
              false,
              false,
              isBefore ? methods[i].getBeforeGroups() : new String[0],
              new String[0],
              null,
              instance);
    }

    return result;
  }

  public static ITestNGMethod[] createAfterConfigurationMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder,
      boolean isBefore,
      Object instance) {
    return Arrays.stream(methods)
        .parallel()
        .map(
            m ->
                new ConfigurationMethod(
                    objectFactory,
                    m.getConstructorOrMethod(),
                    annotationFinder,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    new String[0],
                    isBefore ? m.getBeforeGroups() : m.getAfterGroups(),
                    null,
                    instance))
        .toArray(ITestNGMethod[]::new);
  }

  public static ITestNGMethod[] createTestMethodConfigurationMethods(
      ITestObjectFactory objectFactory,
      ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder,
      boolean isBefore,
      XmlTest xmlTest,
      Object instance) {
    return createMethods(
        objectFactory,
        methods,
        annotationFinder,
        false,
        false,
        false,
        false,
        false,
        false,
        isBefore,
        !isBefore,
        xmlTest,
        instance);
  }

  /** @return Returns the isAfterClassConfiguration. */
  @Override
  public boolean isAfterClassConfiguration() {
    return m_isAfterClassConfiguration;
  }
  /** @return Returns the isAfterMethodConfiguration. */
  @Override
  public boolean isAfterMethodConfiguration() {
    return m_isAfterMethodConfiguration;
  }
  /** @return Returns the isBeforeClassConfiguration. */
  @Override
  public boolean isBeforeClassConfiguration() {
    return m_isBeforeClassConfiguration;
  }
  /** @return Returns the isBeforeMethodConfiguration. */
  @Override
  public boolean isBeforeMethodConfiguration() {
    return m_isBeforeMethodConfiguration;
  }

  /** @return Returns the isAfterSuiteConfiguration. */
  @Override
  public boolean isAfterSuiteConfiguration() {
    return m_isAfterSuiteConfiguration;
  }

  /** @return Returns the isBeforeSuiteConfiguration. */
  @Override
  public boolean isBeforeSuiteConfiguration() {
    return m_isBeforeSuiteConfiguration;
  }

  @Override
  public boolean isBeforeTestConfiguration() {
    return m_isBeforeTestConfiguration;
  }

  @Override
  public boolean isAfterTestConfiguration() {
    return m_isAfterTestConfiguration;
  }

  @Override
  public boolean isBeforeGroupsConfiguration() {
    return m_beforeGroups != null && m_beforeGroups.length > 0;
  }

  @Override
  public boolean hasBeforeGroupsConfiguration() {
    return this.m_isBeforeGroupsConfiguration;
  }

  @Override
  public boolean hasAfterGroupsConfiguration() {
    return this.m_isAfterGroupsConfiguration;
  }

  @Override
  public boolean isAfterGroupsConfiguration() {
    return m_afterGroups != null && m_afterGroups.length > 0;
  }

  @Override
  public boolean isIgnoreFailure() {
    return this.m_isIgnoreFailure;
  }

  private boolean inheritGroupsFromTestClass() {
    return m_inheritGroupsFromTestClass;
  }

  private void init() {
    IConfigurationAnnotation annotation =
        AnnotationHelper.findConfiguration(m_annotationFinder, m_method.getMethod());
    if (annotation != null) {
      m_inheritGroupsFromTestClass = annotation.getInheritGroups();
      setEnabled(annotation.getEnabled());
      setDescription(annotation.getDescription());
    }

    if (annotation != null && annotation.isFakeConfiguration()) {
      if (annotation.getBeforeSuite()) {
        initGroups(IBeforeSuite.class);
      }
      if (annotation.getAfterSuite()) {
        initGroups(IAfterSuite.class);
      }
      if (annotation.getBeforeTest()) {
        initGroups(IBeforeTest.class);
      }
      if (annotation.getAfterTest()) {
        initGroups(IAfterTest.class);
      }
      this.m_isBeforeGroupsConfiguration = annotation.isBeforeGroups();
      this.m_isAfterGroupsConfiguration = annotation.isAfterGroups();

      if (annotation.getBeforeGroups().length != 0) {
        initBeforeAfterGroups(IBeforeGroups.class, annotation.getBeforeGroups());
      }
      if (annotation.getAfterGroups().length != 0) {
        initBeforeAfterGroups(IAfterGroups.class, annotation.getAfterGroups());
      }
      if (annotation.getBeforeTestClass()) {
        initGroups(IBeforeClass.class);
      }
      if (annotation.getAfterTestClass()) {
        initGroups(IAfterClass.class);
      }
      if (annotation.getBeforeTestMethod()) {
        initGroups(IBeforeMethod.class);
      }
      if (annotation.getAfterTestMethod()) {
        initGroups(IAfterMethod.class);
      }
    } else {
      initGroups(IConfigurationAnnotation.class);
    }

    // If this configuration method has inherit-groups=true, add the groups
    // defined in the @Test class
    if (inheritGroupsFromTestClass()) {
      ITestAnnotation classAnnotation =
          m_annotationFinder.findAnnotation(m_methodClass, ITestAnnotation.class);
      if (classAnnotation != null) {
        String[] groups = classAnnotation.getGroups();
        Map<String, String> newGroups = Maps.newHashMap();
        for (String g : getGroups()) {
          newGroups.put(g, g);
        }
        if (groups != null) {
          for (String g : groups) {
            newGroups.put(g, g);
          }
          setGroups(newGroups.values().toArray(new String[0]));
        }
      }
    }

    if (annotation != null) {
      setTimeOut(annotation.getTimeOut());
    }
  }

  @Override
  public ConfigurationMethod clone() {
    ConfigurationMethod clone =
        new ConfigurationMethod(
            m_objectFactory,
            getConstructorOrMethod(),
            getAnnotationFinder(),
            isBeforeSuiteConfiguration(),
            isAfterSuiteConfiguration(),
            isBeforeTestConfiguration(),
            isAfterTestConfiguration(),
            isBeforeClassConfiguration(),
            isAfterClassConfiguration(),
            isBeforeMethodConfiguration(),
            isAfterMethodConfiguration(),
            isIgnoreFailure(),
            getBeforeGroups(),
            getAfterGroups(),
            false /* do not call init() */,
            getFactoryMethodParamsInfo());
    clone.m_testClass = getTestClass();
    clone.setDate(getDate());
    clone.setGroups(getGroups());
    clone.setGroupsDependedUpon(getGroupsDependedUpon(), Collections.emptyList());
    clone.setMethodsDependedUpon(getMethodsDependedUpon());
    clone.setAlwaysRun(isAlwaysRun());
    clone.setMissingGroup(getMissingGroup());
    clone.setDescription(getDescription());
    clone.setEnabled(getEnabled());
    clone.setParameterInvocationCount(getParameterInvocationCount());
    clone.m_inheritGroupsFromTestClass = inheritGroupsFromTestClass();

    return clone;
  }

  public boolean isFirstTimeOnly() {
    boolean result = false;
    IAnnotation before =
        m_annotationFinder.findAnnotation(getConstructorOrMethod(), IBeforeMethod.class);
    if (before != null) {
      result = ((ConfigurationAnnotation) before).isFirstTimeOnly();
    }
    return result;
  }

  public boolean isLastTimeOnly() {
    boolean result = false;
    IAnnotation before =
        m_annotationFinder.findAnnotation(getConstructorOrMethod(), IAfterMethod.class);
    if (before != null) {
      result = ((ConfigurationAnnotation) before).isLastTimeOnly();
    }
    return result;
  }

  public String[] getGroupFilters() {
    IBaseBeforeAfterMethod beforeAfter;
    beforeAfter = m_annotationFinder.findAnnotation(getConstructorOrMethod(), IBeforeMethod.class);
    if (beforeAfter == null) {
      beforeAfter = m_annotationFinder.findAnnotation(getConstructorOrMethod(), IAfterMethod.class);
    }
    if (beforeAfter == null) {
      return new String[0];
    }
    return beforeAfter.getGroupFilters();
  }
}
