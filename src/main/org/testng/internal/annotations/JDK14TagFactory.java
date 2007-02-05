package org.testng.internal.annotations;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;

import com.thoughtworks.qdox.model.AbstractInheritableJavaEntity;
import com.thoughtworks.qdox.model.DocletTag;

/**
 * This class creates implementations of IAnnotations based on the JavaDoc tag
 * that was found on the Java element.
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK14TagFactory {
  // Maps tags and IAnnotation classes
  private static Map m_annotationMap = new HashMap();
  
  // Some of these variables are used in the Eclipse plug-in
  static public final String CONFIGURATION = "testng.configuration";
  static public final String FACTORY = "testng.factory";
  static public final String TEST = "testng.test";
  static public final String EXPECTED_EXCEPTIONS = "testng.expected-exceptions";
  static public final String DATA_PROVIDER = "testng.data-provider";
  static public final String PARAMETERS = "testng.parameters";  
  static public final String BEFORE_SUITE = "testng.before-suite";
  static public final String AFTER_SUITE = "testng.after-suite";
  static public final String BEFORE_TEST = "testng.before-test";
  static public final String AFTER_TEST = "testng.after-test";
  static public final String BEFORE_GROUPS = "testng.before-groups";
  static public final String AFTER_GROUPS = "testng.after-groups";
  static public final String BEFORE_CLASS = "testng.before-class";
  static public final String AFTER_CLASS = "testng.after-class";
  static public final String BEFORE_METHOD = "testng.before-method";
  static public final String AFTER_METHOD = "testng.after-method";
  
  
  static {
    m_annotationMap.put(IConfiguration.class, CONFIGURATION);
    m_annotationMap.put(IDataProvider.class, DATA_PROVIDER);
    m_annotationMap.put(IExpectedExceptions.class, EXPECTED_EXCEPTIONS);
    m_annotationMap.put(IFactory.class, FACTORY);
    m_annotationMap.put(IParameters.class, PARAMETERS);
    m_annotationMap.put(ITest.class, TEST);
    m_annotationMap.put(IBeforeSuite.class, BEFORE_SUITE);
    m_annotationMap.put(IAfterSuite.class, AFTER_SUITE);
    m_annotationMap.put(IBeforeTest.class, BEFORE_TEST);
    m_annotationMap.put(IAfterTest.class, AFTER_TEST);
    m_annotationMap.put(IBeforeGroups.class, BEFORE_GROUPS);
    m_annotationMap.put(IAfterGroups.class, AFTER_GROUPS);
    m_annotationMap.put(IBeforeClass.class, BEFORE_CLASS);
    m_annotationMap.put(IAfterClass.class, AFTER_CLASS);
    m_annotationMap.put(IBeforeMethod.class, BEFORE_METHOD);
    m_annotationMap.put(IAfterMethod.class, AFTER_METHOD);
  }
  
  public IAnnotation createTag(Class annotationClass, 
      AbstractInheritableJavaEntity entity,
      IAnnotationTransformer transformer) 
  {
    IAnnotation result = null;
    String tag = getTagName(annotationClass);
//    ppp("TAG FOR " + annotationClass + " = " + tag);
//    ppp("# OF TAGS:" + entity.getTags().length);
    DocletTag dt = entity.getTagByName(tag, true /* superclasses */);
//    ppp("DOCLET TAG:" + dt);
    if (dt != null) {
      result = createTag(annotationClass, dt, transformer);
    }
    return result;
  }
  
  public String getTagName(Class annotationClass) {
    String result = (String) m_annotationMap.get(annotationClass); 
    Assert.assertNotNull(result, "No tag found for " + annotationClass);
    return result;
  }

  private IAnnotation createTag(Class annotationClass, DocletTag dt,
      IAnnotationTransformer transformer) 
  {
    IAnnotation result = null;
    if (annotationClass == IConfiguration.class) {
      result = createConfigurationTag(dt);
    }
    else if (annotationClass == IDataProvider.class) {
      result = createDataProviderTag(dt);
    }
    else if (annotationClass == IExpectedExceptions.class) {
      result = createExpectedExceptionsTag(dt);
    }
    else if (annotationClass == IFactory.class) {
      result = createFactoryTag(dt);
    }
    else if (annotationClass == IParameters.class) {
      result = createParametersTag(dt);
    }
    else if (annotationClass == ITest.class) {
      result = createTestTag(dt, transformer);
    }
    else if (annotationClass == IBeforeSuite.class || annotationClass == IAfterSuite.class || 
        annotationClass == IBeforeTest.class || annotationClass == IAfterTest.class ||
        annotationClass == IBeforeGroups.class || annotationClass == IAfterGroups.class ||
        annotationClass == IBeforeClass.class || annotationClass == IAfterClass.class || 
        annotationClass == IBeforeMethod.class || annotationClass == IAfterMethod.class) 
    {
      result = maybeCreateNewConfigurationTag(annotationClass, dt);
    }
    else {
      ppp("UNKNOWN ANNOTATION CLASS " + annotationClass);
    }
    
    return result;
  }

  private IAnnotation maybeCreateNewConfigurationTag(Class cls, DocletTag dt) {
    IConfiguration result = new ConfigurationAnnotation();

    boolean alwaysRun = Converter.getBoolean(dt.getNamedParameter("alwaysRun"), result.getAlwaysRun());
    String[] dependsOnGroups = Converter.getStringArray(dt.getNamedParameter("dependsOnGroups"), result.getDependsOnGroups());
    String[] dependsOnMethods = Converter.getStringArray(dt.getNamedParameter("dependsOnMethods"), result.getDependsOnMethods());
    String description = Converter.getString(dt.getNamedParameter("description"), result.getDescription());
    boolean enabled = Converter.getBoolean(dt.getNamedParameter("enabled"), result.getEnabled());
    String[] groups = Converter.getStringArray(dt.getNamedParameter("groups"), result.getGroups());
    boolean inheritGroups = Converter.getBoolean(dt.getNamedParameter("inheritGroups"), result.getInheritGroups());
    String[] beforeGroups = Converter.getStringArray(dt.getNamedParameter("before-groups"), groups);
    String[] afterGroups = Converter.getStringArray(dt.getNamedParameter("after-groups"), groups);
//    String parameters = Converter.getString(dt.getNamedParameter("parameters"), result.getParameters());

    if (BEFORE_SUITE.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          true, false,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null /* parameters */);
    }
    else if (AFTER_SUITE.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, true,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (BEFORE_TEST.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          true, false, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (AFTER_TEST.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, true, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (BEFORE_GROUPS.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, false, 
          beforeGroups, new String[0], 
          false, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (AFTER_GROUPS.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, false, 
          new String[0], afterGroups, 
          false, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (BEFORE_CLASS.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, false, 
          new String[0], new String[0], 
          true, false, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (AFTER_CLASS.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, false, 
          new String[0], new String[0], 
          false, true, 
          false, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (BEFORE_METHOD.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          true, false,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    else if (AFTER_METHOD.equals(dt.getName())) {
      result = createConfigurationTag(cls, dt,
          false, false,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          false, true,
          alwaysRun,
          dependsOnGroups,
          dependsOnMethods,
          description,
          enabled,
          groups,
          inheritGroups,
          null);
    }
    
    return result;
  }

  private IConfiguration createConfigurationTag(Class cls, DocletTag dt, 
      boolean beforeSuite, boolean afterSuite,
      boolean beforeTest, boolean afterTest,
      String[] beforeGroups, String[] afterGroups,
      boolean beforeClass, boolean afterClass,
      boolean beforeMethod, boolean afterMethod,
      boolean alwaysRun,
      String[] dependsOnGroups, String[] dependsOnMethods,
      String description, boolean enabled, String[] groups,
      boolean inheritGroups, String[] parameters)
  {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    result.setFakeConfiguration(true);
    result.setBeforeSuite(beforeSuite);
    result.setAfterSuite(afterSuite);
    result.setBeforeTest(beforeTest);
    result.setAfterTest(afterTest);
    result.setBeforeGroups(beforeGroups);
    result.setAfterGroups(afterGroups);
    result.setBeforeTestClass(beforeClass);
    result.setAfterTestClass(afterClass);
    result.setBeforeTestMethod(beforeTest);
    result.setAfterTestMethod(afterTest);
    result.setParameters(parameters);    
    result.setEnabled(enabled);
    result.setGroups(groups);
    result.setDependsOnGroups(dependsOnGroups);
    result.setDependsOnMethods(dependsOnMethods);
    result.setAlwaysRun(alwaysRun);    
    result.setInheritGroups(inheritGroups);
    result.setDescription(description);    
    
    return result;
  }

  private IAnnotation createTestTag(DocletTag dt, IAnnotationTransformer transformer) {
    TestAnnotation result = new TestAnnotation();
    result.setEnabled(Converter.getBoolean(dt.getNamedParameter("enabled"),
        result.getEnabled()));
    result.setGroups(Converter.getStringArray(dt.getNamedParameter("groups"),
        result.getGroups()));
    result.setParameters(Converter.getStringArray(dt.getNamedParameter("parameters"),
        result.getParameters()));
    result.setDependsOnGroups(Converter.getStringArray(dt.getNamedParameter("dependsOnGroups"),
        result.getDependsOnGroups()));
    result.setDependsOnMethods(Converter.getStringArray(dt.getNamedParameter("dependsOnMethods"),
        result.getDependsOnMethods()));
    result.setTimeOut(Converter.getLong(dt.getNamedParameter("timeOut"),
        result.getTimeOut()));
    result.setInvocationCount(Converter.getInt(dt.getNamedParameter("invocationCount"),
        result.getInvocationCount()));
    result.setThreadPoolSize(Converter.getInt(dt.getNamedParameter("threadPoolSize"),
        result.getThreadPoolSize()));
    result.setSuccessPercentage(Converter.getInt(dt.getNamedParameter("successPercentage"),
        result.getSuccessPercentage()));
    result.setDataProvider(Converter.getString(dt.getNamedParameter("dataProvider"),
        result.getDataProvider()));
    result.setAlwaysRun(Converter.getBoolean(dt.getNamedParameter("alwaysRun"),
        result.getAlwaysRun()));    
    result.setDescription(Converter.getString(dt.getNamedParameter("description"),
        result.getDescription()));
    result.setExpectedExceptions(Converter.getClassArray(dt.getNamedParameter("expectedExceptions"),
        result.getExpectedExceptions()));
    result.setSuiteName(Converter.getString(dt.getNamedParameter("suiteName"),
        result.getSuiteName()));
    result.setTestName(Converter.getString(dt.getNamedParameter("testName"),
        result.getTestName()));
    result.setSequential(Converter.getBoolean(dt.getNamedParameter("sequential"),
        result.getSequential()));
    result.setDataProviderClass(Converter.getClass(dt.getNamedParameter("dataProviderClass")));
    
    return result;
  }
  
  private IAnnotation createConfigurationTag(DocletTag dt) {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    result.setBeforeTestClass(Converter.getBoolean(dt.getNamedParameter("beforeTestClass"),
        result.getBeforeTestClass()));
    result.setAfterTestClass(Converter.getBoolean(dt.getNamedParameter("afterTestClass"),
        result.getAfterTestClass()));
    result.setBeforeTestMethod(Converter.getBoolean(dt.getNamedParameter("beforeTestMethod"),
        result.getBeforeTestMethod()));
    result.setAfterTestMethod(Converter.getBoolean(dt.getNamedParameter("afterTestMethod"),
        result.getAfterTestMethod()));
    result.setBeforeTest(Converter.getBoolean(dt.getNamedParameter("beforeTest"),
        result.getBeforeTest()));
    result.setAfterTest(Converter.getBoolean(dt.getNamedParameter("afterTest"),
        result.getAfterTest()));
    result.setAfterSuite(Converter.getBoolean(dt.getNamedParameter("afterSuite"),
        result.getAfterSuite()));
    result.setBeforeSuite(Converter.getBoolean(dt.getNamedParameter("beforeSuite"),
        result.getBeforeSuite()));
    result.setBeforeGroups(Converter.getStringArray(dt.getNamedParameter("beforeGroups"),
        result.getBeforeGroups()));
    result.setAfterGroups(Converter.getStringArray(dt.getNamedParameter("afterGroups"),
        result.getAfterGroups()));
    result.setParameters(Converter.getStringArray(dt.getNamedParameter("parameters"),
        result.getParameters()));    
    result.setEnabled(Converter.getBoolean(dt.getNamedParameter("enabled"),
        result.getEnabled()));
    result.setGroups(Converter.getStringArray(dt.getNamedParameter("groups"),
        result.getGroups()));
    result.setDependsOnGroups(Converter.getStringArray(dt.getNamedParameter("dependsOnGroups"),
        result.getDependsOnGroups()));
    result.setDependsOnMethods(Converter.getStringArray(dt.getNamedParameter("dependsOnMethods"),
        result.getDependsOnMethods()));
    result.setAlwaysRun(Converter.getBoolean(dt.getNamedParameter("alwaysRun"),
        result.getAlwaysRun()));    
    result.setInheritGroups(Converter.getBoolean(dt.getNamedParameter("inheritGroups"),
        result.getInheritGroups()));
    result.setDescription(Converter.getString(dt.getNamedParameter("description"),
        result.getDescription()));    
    
    return result;
  }  
  
  private IAnnotation createDataProviderTag(DocletTag dt) {
    DataProviderAnnotation result = new DataProviderAnnotation();
    result.setName(Converter.getString(dt.getNamedParameter("name"), result.getName()));
    
    return result;
  }

  private IAnnotation createExpectedExceptionsTag(DocletTag dt) {
    ExpectedExceptionsAnnotation result = new ExpectedExceptionsAnnotation();
    result.setValue(Converter.getClassArray(dt.getNamedParameter("value"),
        result.getValue()));

    return result;
  }

  private IAnnotation createParametersTag(DocletTag dt) {
    ParametersAnnotation result = new ParametersAnnotation();
    result.setValue(Converter.getStringArray(dt.getNamedParameter("value"),
        result.getValue()));
    
    return result;
  }
  
  private IAnnotation createFactoryTag(DocletTag dt) {
    FactoryAnnotation result = new FactoryAnnotation();
    result.setParameters(Converter.getStringArray(dt.getNamedParameter("parameters"),
        result.getParameters()));
    result.setDataProvider(Converter.getString(dt.getNamedParameter("dataProvider"),
        result.getDataProvider()));

    return result;
  }

  private static void ppp(String s) {
    System.out.println("[JDK14TagFactory] " + s);
  }

}
