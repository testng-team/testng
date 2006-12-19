package org.testng.internal;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IConfiguration;
import org.testng.internal.annotations.ITest;
import org.testng.internal.mix.RequiresConfiguration;

/**
 * The default strategy for finding test methods:  look up
 * annotations @Test in front of methods.
 *
 * @author Cedric Beust, May 3, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 * @param <ITestNGMetthod>
 */
public class TestNGMethodFinder<ITestNGMetthod> implements ITestMethodFinder {
  private static final int BEFORE_SUITE = 1;
  private static final int AFTER_SUITE = 2;
  private static final int BEFORE_TEST = 3;
  private static final int AFTER_TEST = 4;
  private static final int BEFORE_CLASS = 5;
  private static final int AFTER_CLASS = 6;
  private static final int BEFORE_TESTMETHOD = 7;
  private static final int AFTER_TESTMETHOD = 8;
  private static final int BEFORE_GROUPS = 9;
  private static final int AFTER_GROUPS = 10;
    
  private RunInfo m_runInfo = null;
  private IAnnotationFinder m_annotationFinder = null;

  public TestNGMethodFinder(RunInfo runInfo,
                            IAnnotationFinder annotationFinder)
  {
    m_runInfo = runInfo;
    m_annotationFinder = annotationFinder;
  }

  public ITestNGMethod[] getTestMethods(Class clazz) {
    return AnnotationHelper.findMethodsWithAnnotation(
        clazz, ITest.class, m_annotationFinder);
  }

  public ITestNGMethod[] getBeforeClassMethods(Class cls) {
    return findAllConfiguration(cls, BEFORE_CLASS);
//    return findConfiguration(cls, false, true, false /* isSuite */);
  }

  public ITestNGMethod[] getAfterClassMethods(Class cls) {
    return findConfiguration(cls, AFTER_CLASS);
//    return findConfiguration(cls, false /* method */, false, /* before */
//                             false /* isSuite */);
  }

  public ITestNGMethod[] getBeforeTestMethods(Class cls) {
    return findConfiguration(cls, BEFORE_TESTMETHOD);
//    return findConfiguration(cls, true /* method */, true /* before */,
//                             false /* isSuite */);
  }

  public ITestNGMethod[] getAfterTestMethods(Class cls) {
    return findConfiguration(cls, AFTER_TESTMETHOD);
//    return findConfiguration(cls, true /* method */, false /* before */,
//                             false /* isSuite */);
  }

  public ITestNGMethod[] getBeforeSuiteMethods(Class cls) {
    return findConfiguration(cls, BEFORE_SUITE);
//    return findConfiguration(cls,
//                             false /*method */, true /* before */,
//                             true /* suite */);
  }

  public ITestNGMethod[] getAfterSuiteMethods(Class cls) {
    return findConfiguration(cls, AFTER_SUITE);
//    return findConfiguration(cls, false /* method */, false /* before */,
//                             true /* suite */);
  }

  public ITestNGMethod[] getBeforeTestConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, BEFORE_TEST);
  }

  public ITestNGMethod[] getAfterTestConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, AFTER_TEST);
  }
  
  public ITestNGMethod[] getBeforeGroupsConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, BEFORE_GROUPS);    
  }

  public ITestNGMethod[] getAfterGroupsConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, AFTER_GROUPS);        
  }

  private ITestNGMethod[] findAllConfiguration(final Class clazz, final int configurationType) {
    RequiresConfiguration reqConf= (RequiresConfiguration) clazz.getAnnotation(RequiresConfiguration.class);
    List<ITestNGMethod> result= new ArrayList<ITestNGMethod>();
    if(null != reqConf) {
      Class[] confMixins= reqConf.value();
      for(Class cls: confMixins) {
        result.addAll(Arrays.asList(findConfiguration(cls, configurationType)));
      }
    }
    result.addAll(Arrays.asList(findConfiguration(clazz, configurationType)));
    
    return result.toArray(new ITestNGMethod[result.size()]);
  }
  
  private ITestNGMethod[] findConfiguration(final Class clazz, final int configurationType) {
    List<ITestNGMethod> vResult = new ArrayList<ITestNGMethod>();
    
    Set<Method> methods = ClassHelper.getAvailableMethods(clazz);

    for(Method m : methods) {
      IConfiguration configuration = AnnotationHelper.findConfiguration(m_annotationFinder, m);

      if(null == configuration) {
        continue;
      }
      
      boolean create = false;
      boolean isBeforeSuite = false;
      boolean isAfterSuite = false;
      boolean isBeforeTest = false;
      boolean isAfterTest = false;
      boolean isBeforeClass = false;
      boolean isAfterClass = false;
      boolean isBeforeTestMethod = false;
      boolean isAfterTestMethod = false;
      String[] beforeGroups = null;
      String[] afterGroups = null;
      
      switch(configurationType) {
        case BEFORE_SUITE:
          create = configuration.getBeforeSuite();
          isBeforeSuite = true;
          break;
        case AFTER_SUITE:
          create = configuration.getAfterSuite();
          isAfterSuite = true;
          break;
        case BEFORE_TEST:
          create = configuration.getBeforeTest();
          isBeforeTest = true;
          break;
        case AFTER_TEST:
          create = configuration.getAfterTest();
          isAfterTest = true;
          break;
        case BEFORE_CLASS:
          create = configuration.getBeforeTestClass();
          isBeforeClass = true;
          break;
        case AFTER_CLASS:
          create = configuration.getAfterTestClass();
          isAfterClass = true;
          break;
        case BEFORE_TESTMETHOD:
          create = configuration.getBeforeTestMethod();
          isBeforeTestMethod = true;
          break;
        case AFTER_TESTMETHOD:
          create = configuration.getAfterTestMethod();
          isAfterTestMethod = true;
          break;
        case BEFORE_GROUPS:
          beforeGroups = configuration.getBeforeGroups();
          create = beforeGroups.length > 0;
          isBeforeTestMethod = true;
          break;
        case AFTER_GROUPS:
          afterGroups = configuration.getAfterGroups();
          create = afterGroups.length > 0;
          isBeforeTestMethod = true;
          break;
      }
    
      if(create) {
        addConfigurationMethod(clazz, 
                               vResult, 
                               m, 
                               isBeforeSuite, 
                               isAfterSuite, 
                               isBeforeTest, 
                               isAfterTest, 
                               isBeforeClass, 
                               isAfterClass, 
                               isBeforeTestMethod, 
                               isAfterTestMethod,
                               beforeGroups,
                               afterGroups);
      }
    }

    ITestNGMethod[] unorderedResult = vResult.toArray(new ITestNGMethod[vResult.size()]);

    List<ITestNGMethod> excludedMethods = new ArrayList<ITestNGMethod>();
    boolean unique = 
      configurationType == BEFORE_SUITE || configurationType == AFTER_SUITE;
    ITestNGMethod[] tmResult = MethodHelper.collectAndOrderConfigurationMethods(unorderedResult,
                                                                                m_runInfo,
                                                                                m_annotationFinder,
                                                                                unique,
                                                                                excludedMethods);

    return tmResult;
  }
   


  private void addConfigurationMethod(Class clazz,
                                      List<ITestNGMethod> results,
                                      Method method,
                                      boolean isBeforeSuite,
                                      boolean isAfterSuite,
                                      boolean isBeforeTest,
                                      boolean isAfterTest,
                                      boolean isBeforeClass,
                                      boolean isAfterClass,
                                      boolean isBeforeTestMethod,
                                      boolean isAfterTestMethod,
                                      String[] beforeGroups,
                                      String[] afterGroups) 
  {
    if(method.getDeclaringClass().isAssignableFrom(clazz)) {
      ITestNGMethod confMethod = new ConfigurationMethod(method,
                                                         m_annotationFinder,
                                                         isBeforeSuite,
                                                         isAfterSuite,
                                                         isBeforeTest,
                                                         isAfterTest,
                                                         isBeforeClass,
                                                         isAfterClass,
                                                         isBeforeTestMethod,
                                                         isAfterTestMethod,
                                                         beforeGroups,
                                                         afterGroups);
      results.add(confMethod);
    }
  }

  private static void ppp(String s) {
    System.out.println("[DefaultTestMethodFinder] " + s);
  }
  

}
