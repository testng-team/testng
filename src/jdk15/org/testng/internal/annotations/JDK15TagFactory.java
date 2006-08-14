package org.testng.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.testng.TestNGException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Configuration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class creates implementations of IAnnotations based on the JDK5
 * annotation that was found on the Java element.
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK15TagFactory {
  public IAnnotation createTag(Class cls, Annotation a, Class annotationClass) {
    IAnnotation result = null;

    if (a != null) {
      if (annotationClass == IConfiguration.class) {
        result = createConfigurationTag(cls, a);
      }
      else if (annotationClass == IDataProvider.class) {      
        result = createDataProviderTag(a);
      }
      else if (annotationClass == IExpectedExceptions.class) {      
        result = createExpectedExceptionsTag(a);
      }
      else if (annotationClass == IFactory.class) {
        result = createFactoryTag(a);
      }
      else if (annotationClass == IParameters.class) {      
        result = createParametersTag(a);
      }
      else if (annotationClass == ITest.class) {      
        result = createTestTag(cls, a);
      }
      else if (annotationClass == IBeforeSuite.class || annotationClass == IAfterSuite.class || 
          annotationClass == IBeforeTest.class || annotationClass == IAfterTest.class ||
          annotationClass == IBeforeGroups.class || annotationClass == IAfterGroups.class ||
          annotationClass == IBeforeClass.class || annotationClass == IAfterClass.class || 
          annotationClass == IBeforeMethod.class || annotationClass == IAfterMethod.class) 
      {
        result = maybeCreateNewConfigurationTag(cls, a, annotationClass);
      }

      else {
        throw new TestNGException("Unknown annotation requested:" + annotationClass);
      }
    }

    return result;
  }

  private IAnnotation maybeCreateNewConfigurationTag(Class cls, Annotation a,
      Class annotationClass) 
  {
    IAnnotation result = null;
    
    if (annotationClass == IBeforeSuite.class) {
      BeforeSuite bs = (BeforeSuite) a;
      result = createConfigurationTag(cls, a, 
          true, false,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IAfterSuite.class) {
      AfterSuite bs = (AfterSuite) a;
      result = createConfigurationTag(cls, a, 
          false, true,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IBeforeTest.class) {
      BeforeTest bs = (BeforeTest) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          true, false, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IAfterTest.class) {
      AfterTest bs = (AfterTest) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, true, 
          new String[0], new String[0], 
          false, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IBeforeGroups.class) {
      BeforeGroups bs = (BeforeGroups) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, false, 
          bs.value(), new String[0], 
          false, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IAfterGroups.class) {
      AfterGroups bs = (AfterGroups) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, false, 
          new String[0], bs.value(),
          false, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IBeforeClass.class) {
      BeforeClass bs = (BeforeClass) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, false, 
          new String[0], new String[0], 
          true, false, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IAfterClass.class) {
      AfterClass bs = (AfterClass) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, false, 
          new String[0], new String[0], 
          false, true, 
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IBeforeMethod.class) {
      BeforeMethod bs = (BeforeMethod) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          true, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    else if (annotationClass == IAfterMethod.class) {
      AfterMethod bs = (AfterMethod) a;
      result = createConfigurationTag(cls, a, 
          false, false,
          false, false, 
          new String[0], new String[0], 
          false, false, 
          false, true,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null);
    }
    
    return result;
  }

  private IAnnotation createConfigurationTag(Class cls, Annotation a) {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    Configuration c = (Configuration) a;
    result.setBeforeTestClass(c.beforeTestClass());
    result.setAfterTestClass(c.afterTestClass());
    result.setBeforeTestMethod(c.beforeTestMethod());
    result.setAfterTestMethod(c.afterTestMethod());
    result.setBeforeTest(c.beforeTest());
    result.setAfterTest(c.afterTest());
    result.setBeforeSuite(c.beforeSuite());
    result.setAfterSuite(c.afterSuite());
    result.setBeforeGroups(c.beforeGroups());
    result.setAfterGroups(c.afterGroups());
    result.setParameters(c.parameters());    
    result.setEnabled(c.enabled());
    
    result.setGroups(join(c.groups(), findInheritedStringArray(cls, Test.class, "groups")));
    result.setDependsOnGroups(c.dependsOnGroups());
    result.setDependsOnMethods(c.dependsOnMethods());
    result.setAlwaysRun(c.alwaysRun());    
    result.setInheritGroups(c.inheritGroups());    
    result.setDescription(c.description());

    return result;
  }
  
  private IAnnotation createConfigurationTag(Class cls, Annotation a,
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
    result.setBeforeTestClass(beforeTest);
    result.setAfterTestClass(afterTest);
    result.setBeforeTestClass(beforeClass);
    result.setAfterTestClass(afterClass);
    result.setBeforeGroups(beforeGroups);
    result.setAfterGroups(afterGroups);    
    result.setBeforeTestMethod(beforeMethod);
    result.setAfterTestMethod(afterMethod);

    result.setAlwaysRun(alwaysRun);
    result.setDependsOnGroups(dependsOnGroups);
    result.setDependsOnMethods(dependsOnMethods);
    result.setDescription(description);
    result.setEnabled(enabled);
    result.setGroups(groups);
    result.setInheritGroups(inheritGroups);
    result.setParameters(parameters);
    
    return result;
  }

  private IAnnotation createDataProviderTag(Annotation a) {
    DataProviderAnnotation result = new DataProviderAnnotation();
    DataProvider c = (DataProvider) a;
    result.setName(c.name());
    
    return result;
  }

  private IAnnotation createExpectedExceptionsTag(Annotation a) {
    ExpectedExceptionsAnnotation result = new ExpectedExceptionsAnnotation ();
    ExpectedExceptions c = (ExpectedExceptions ) a;
    result.setValue(c.value());
    
    return result;
  }

  private IAnnotation createFactoryTag(Annotation a) {
    FactoryAnnotation result = new FactoryAnnotation();
    Factory c = (Factory) a;
    result.setParameters(c.parameters());
    
    return result;
  }

  private IAnnotation createParametersTag(Annotation a) {
    ParametersAnnotation result = new ParametersAnnotation();
    Parameters c = (Parameters) a;
    result.setValue(c.value());
    
    return result;
  }
  
  private IAnnotation createTestTag(Class cls, Annotation a) {
    TestAnnotation result = new TestAnnotation();
    Test test = (Test) a;
    
    result.setEnabled(test.enabled());
    result.setGroups(join(test.groups(), findInheritedStringArray(cls, Test.class, "groups")));
    result.setParameters(test.parameters());
    result.setDependsOnGroups(join(test.dependsOnGroups(), 
        findInheritedStringArray(cls, Test.class, "dependsOnGroups")));
    result.setDependsOnMethods(join(test.dependsOnMethods(), 
        findInheritedStringArray(cls, Test.class, "dependsOnMethods")));
    result.setTimeOut(test.timeOut());
    result.setInvocationCount(test.invocationCount());
    result.setThreadPoolSize(test.threadPoolSize());
    result.setSuccessPercentage(test.successPercentage());
    result.setDataProvider(test.dataProvider());
    result.setAlwaysRun(test.alwaysRun());
    result.setDescription(test.description());
    result.setExpectedExceptions(test.expectedExceptions());
    result.setSuiteName(test.suiteName());
    result.setTestName(test.testName());
    result.setSequential(test.sequential());

    return result;
  }

  private String[] join(String[] strings, String[] strings2) {
    Map<String, String> vResult = new HashMap<String, String>();
    for (String s : strings) {
      vResult.put(s, s);
    }
    for (String s : strings2) {
      vResult.put(s, s);
    }
    
    return vResult.keySet().toArray(new String[vResult.size()]);
  }

  private String[] findInheritedStringArray(Class cls, Class annotationClass,
      String methodName)
  {
    Map<String, String> vResult = new HashMap<String, String>();
    
    while (cls != Object.class) {
      Annotation annotation = cls.getAnnotation(annotationClass);
      if (annotation != null) {
        String[] g = (String[]) invokeMethod(annotation, methodName);
        for (String s : g) {
          vResult.put(s, s);
        }
      }
      cls = cls.getSuperclass();
    }
    
    String[] result = vResult.keySet().toArray(new String[vResult.size()]);
    return result;
  }

//  private Boolean findInheritedBoolean(Class cls, Class annotationClass,
//      String methodName, Boolean result)
//  {
//    Map<String, String> vResult = new HashMap<String, String>();
//    
//    while (cls != Object.class) {
//      Annotation annotation = cls.getAnnotation(annotationClass);
//      if (annotation != null) {
//        return (Boolean) invokeMethod(annotation, methodName);
//      }
//      else {
//        cls = cls.getSuperclass();
//      }
//      
//    }
//    
//    return result;
//  }

  private Object invokeMethod(Annotation test, String methodName) {
    Object result = null;
    try {
      // Note:  we should cache methods already looked up
      Method m = test.getClass().getMethod(methodName, new Class[0]);
      result = m.invoke(test, new Object[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
    }    
    return result;
  }

  private void ppp(String string) {
    System.out.println("[JDK15TagFactory] " + string);
  }

}
