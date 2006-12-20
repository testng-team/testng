package org.testng.internal.annotations;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.testng.ITestNGMethod;
import org.testng.internal.TestNGMethod;
import org.testng.internal.Utils;

/**
 * Helper methods to find @Test and @Configuration tags.  They minimize
 * the amount of casting we need to do.
 * 
 * Created on Dec 20, 2005
 * @author cbeust
 */
public class AnnotationHelper {

  public static ITest findTest(IAnnotationFinder finder, Class cls) {
    return (ITest) finder.findAnnotation(cls, ITest.class);
  }
  
  public static ITest findTest(IAnnotationFinder finder, Method m) {
    return (ITest) finder.findAnnotation(m, ITest.class);
  }
  
  public static ITest findTest(IAnnotationFinder finder, Constructor ctor) {
    return (ITest) finder.findAnnotation(ctor, ITest.class);
  }

  public static IConfiguration findConfiguration(IAnnotationFinder finder, Constructor ctor) {
    IConfiguration result = (IConfiguration) finder.findAnnotation(ctor, IConfiguration.class);
    if (result == null) {
      IConfiguration bs = (IConfiguration) finder.findAnnotation(ctor, IBeforeSuite.class);
      IConfiguration as = (IConfiguration) finder.findAnnotation(ctor, IAfterSuite.class);
      IConfiguration bt = (IConfiguration) finder.findAnnotation(ctor, IBeforeTest.class);
      IConfiguration at = (IConfiguration) finder.findAnnotation(ctor, IAfterTest.class);
      IConfiguration bg = (IConfiguration) finder.findAnnotation(ctor, IBeforeGroups.class);
      IConfiguration ag = (IConfiguration) finder.findAnnotation(ctor, IAfterGroups.class);
      IConfiguration bc = (IConfiguration) finder.findAnnotation(ctor, IBeforeClass.class);
      IConfiguration ac = (IConfiguration) finder.findAnnotation(ctor, IAfterClass.class);
      IConfiguration bm = (IConfiguration) finder.findAnnotation(ctor, IBeforeMethod.class);
      IConfiguration am = (IConfiguration) finder.findAnnotation(ctor, IAfterMethod.class);
      
      if (bs != null || as != null || bt != null || at != null || bg != null || ag != null
          || bc != null || ac != null || bm != null || am != null) 
      {
        result = createConfiguration(bs, as, bt, at, bg, ag, bc, ac, bm, am);
      }    
    }
    
    return result;
  }

  public static IConfiguration findConfiguration(IAnnotationFinder finder, Method m) {
    IConfiguration result = (IConfiguration) finder.findAnnotation(m, IConfiguration.class);
    if (result == null) {
      IConfiguration bs = (IConfiguration) finder.findAnnotation(m, IBeforeSuite.class);
      IConfiguration as = (IConfiguration) finder.findAnnotation(m, IAfterSuite.class);
      IConfiguration bt = (IConfiguration) finder.findAnnotation(m, IBeforeTest.class);
      IConfiguration at = (IConfiguration) finder.findAnnotation(m, IAfterTest.class);
      IConfiguration bg = (IConfiguration) finder.findAnnotation(m, IBeforeGroups.class);
      IConfiguration ag = (IConfiguration) finder.findAnnotation(m, IAfterGroups.class);
      IConfiguration bc = (IConfiguration) finder.findAnnotation(m, IBeforeClass.class);
      IConfiguration ac = (IConfiguration) finder.findAnnotation(m, IAfterClass.class);
      IConfiguration bm = (IConfiguration) finder.findAnnotation(m, IBeforeMethod.class);
      IConfiguration am = (IConfiguration) finder.findAnnotation(m, IAfterMethod.class);
      
      if (bs != null || as != null || bt != null || at != null || bg != null || ag != null
          || bc != null || ac != null || bm != null || am != null) 
      {
        result = createConfiguration(bs, as, bt, at, bg, ag, bc, ac, bm, am);
      }    
    }
    
    return result;
  }
  
  private static IConfiguration createConfiguration(IConfiguration bs, IConfiguration as, 
      IConfiguration bt, IConfiguration at, IConfiguration bg, IConfiguration ag, 
      IConfiguration bc, IConfiguration ac, IConfiguration bm, IConfiguration am) 
  {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    
    if (bs != null) {
      result.setBeforeSuite(true);
      finishInitialize(result, bs);
    }
    if (as != null) {
      result.setAfterSuite(true);
      finishInitialize(result, as);
    }
    if (bt != null) {
      result.setBeforeTest(true);
      finishInitialize(result, bt);
    }
    if (at != null) {
      result.setAfterTest(true);
      finishInitialize(result, at);
    }
    if (bg != null) {
      result.setBeforeGroups(bg.getBeforeGroups());
      finishInitialize(result, bg);
    }
    if (ag != null) {
      result.setAfterGroups(ag.getAfterGroups());
      finishInitialize(result, ag);
    }
    if (bc != null) {
      result.setBeforeTestClass(true);
      finishInitialize(result, bc);
    }
    if (ac != null) {
      result.setAfterTestClass(true);
      finishInitialize(result, ac);
    }
    if (bm != null) {
      result.setBeforeTestMethod(true);
      finishInitialize(result, bm);
    }
    if (am != null) {
      result.setAfterTestMethod(true);
      finishInitialize(result, am);
    }

    return result;
  }
  
  @SuppressWarnings({"deprecation"})
  private static void finishInitialize(ConfigurationAnnotation result, IConfiguration bs) {
    result.setFakeConfiguration(true);
    result.setAlwaysRun(bs.getAlwaysRun());
    result.setDependsOnGroups(bs.getDependsOnGroups());
    result.setDependsOnMethods(bs.getDependsOnGroups());
    result.setDescription(bs.getDescription());
    result.setEnabled(bs.getEnabled());
    result.setGroups(bs.getGroups());
    result.setInheritGroups(bs.getInheritGroups());
    result.setParameters(bs.getParameters());
  }

  private static Class[] ALL_ANNOTATIONS = new Class[] { 
    ITest.class, IConfiguration.class, 
    IBeforeClass.class, IAfterClass.class,
    IBeforeMethod.class, IAfterMethod.class,
    IDataProvider.class, IExpectedExceptions.class, 
    IFactory.class, IParameters.class, 
    IBeforeSuite.class, IAfterSuite.class,
    IBeforeTest.class, IAfterTest.class,
    IBeforeGroups.class, IAfterGroups.class,
  };
  
  public static Class[] CONFIGURATION_CLASSES = new Class[] {
    IConfiguration.class,
    IBeforeSuite.class, IAfterSuite.class,   
    IBeforeTest.class, IAfterTest.class,   
    IBeforeGroups.class, IAfterGroups.class,   
    IBeforeClass.class, IAfterClass.class,  
    IBeforeMethod.class, IAfterMethod.class
  };

  public static Class[] getAllAnnotations() {
    return ALL_ANNOTATIONS;
  };  
  
  /**
   * Delegation method for creating the list of <CODE>ITestMethod</CODE>s to be
   * analysed.
   */
  public static ITestNGMethod[] findMethodsWithAnnotation(Class rootClass, Class annotationClass,
        IAnnotationFinder annotationFinder)
  {
    // Keep a map of the methods we saw so that we ignore a method in a superclass if it's
    // already been seen in a child class
    Map<String, ITestNGMethod> vResult = new HashMap<String, ITestNGMethod>();
    
    try {
      vResult = new HashMap<String, ITestNGMethod>();
//    Class[] classes = rootClass.getTestClasses();
      Class cls = rootClass;
      
      //
      // If the annotation is on the class or superclass, it applies to all public methods
      // except methods marked with @Configuration
      //

      //
      // Otherwise walk through all the methods and keep those
      // that have the annotation
      //
//    for (Class cls : classes) {
        while (null != cls) {
          boolean hasClassAnnotation = isAnnotationPresent(annotationFinder, cls, annotationClass);
          Method[] methods = cls.getDeclaredMethods();
          for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            boolean hasMethodAnnotation = isAnnotationPresent(annotationFinder, m, annotationClass);
            boolean hasTestNGAnnotation =
              isAnnotationPresent(annotationFinder, m, IFactory.class) ||
              isAnnotationPresent(annotationFinder, m, ITest.class) ||
              isAnnotationPresent(annotationFinder, m, CONFIGURATION_CLASSES);
            boolean isPublic = Modifier.isPublic(m.getModifiers());
            if ((isPublic && hasClassAnnotation && (! hasTestNGAnnotation)) || hasMethodAnnotation) {     
              
              // Small hack to allow users to specify @Configuration classes even though
              // a class-level @Test annotation is present.  In this case, don't count
              // that method as a @Test
              if (isAnnotationPresent(annotationFinder, m, IConfiguration.class) &&
                  isAnnotationPresent(annotationFinder, cls, ITest.class)) 
              {
                Utils.log("", 3, "Method " + m + " has a local @Configuration and a class-level @Test." +
                    "This method will only be kept as a @Configuration.");
                    
                continue;
              }
              
              String key = createMethodKey(m);
              if (null == vResult.get(key)) {
                ITestNGMethod tm = new TestNGMethod(/* m.getDeclaringClass(), */ m, annotationFinder);
                vResult.put(key,tm);
              }
            }
          } // for
          // Now explore the superclass
          cls = cls.getSuperclass();
        } // while

    }
    catch (SecurityException e) {
      e.printStackTrace();
    }
    ITestNGMethod[] result = (ITestNGMethod[]) vResult.values().toArray(new ITestNGMethod[vResult.size()]);
      
  //    for (Method m : result) {
  //      ppp("   METHOD FOUND: " + m);
  //    }
      
      return result;
    }

  private static boolean isAnnotationPresent(IAnnotationFinder annotationFinder, 
      Method m, Class[] annotationClasses) 
  {
    for (Class a : annotationClasses) {
      if (annotationFinder.findAnnotation(m, a) != null) {
        return true;
      }
    }
    
    return false;
  }

  private static boolean isAnnotationPresent(IAnnotationFinder annotationFinder, Method m, Class annotationClass) {
    return annotationFinder.findAnnotation(m, annotationClass) != null;
  }

  private static boolean isAnnotationPresent(IAnnotationFinder annotationFinder, Class cls, Class annotationClass) {
    return annotationFinder.findAnnotation(cls, annotationClass) != null;
  }

  /**
   * @return A hashcode representing the name of this method and its parameters,
   * but without its class
   */
  private static String createMethodKey(Method m) {
    StringBuffer result = new StringBuffer(m.getName());
    for (Class paramClass : m.getParameterTypes()) {
      result.append(" ").append(paramClass.toString());
    }
    
    return result.toString();
  }
  
}
