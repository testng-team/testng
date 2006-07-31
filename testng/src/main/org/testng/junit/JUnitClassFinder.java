package org.testng.junit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.testng.IClass;
import org.testng.internal.BaseClassFinder;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/**
 * This class
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JUnitClassFinder extends BaseClassFinder {
  
  public JUnitClassFinder(Class[] classes, XmlTest xmlTest, IAnnotationFinder finder) {
//    {
//      ppp("CREATING CLASS FINDER WITH ");
//      for (Class cls : classes) {
//        ppp("   " + cls);
//      }
//    }
    
    Collection<Class> allClasses = invokeSuite(classes);
    
    for (Class cls : allClasses) {
      IClass ic = findOrCreateIClass(cls, null, xmlTest, finder);
      Utils.log("JUnitClassFinder", 3, "Looking up ClassFinder for " + cls + " " + cls.hashCode() + " " + finder.hashCode());
      if (null != ic) {
        putIClass(cls, ic);
      }
    }
  }
  
  public /* for testing */ static Collection<Class> invokeSuite(Class[] classes) {
    Map<Class, Class> vResult = new HashMap<Class, Class>();
    
    for (Class cls : classes) {
      Class[] allClasses = invokeSuite(cls);
      for (Class o : allClasses) {
        vResult.put(o, o);
      }
    }
    
    return vResult.values();
  }
  
  /**
   * Invoke the suite() method on the given class and return the objects returned
   * by it.  If there is no suite() method, an empty array is returned
   * @param cls
   * @return
   */
  public static Class[] invokeSuite(Class cls) {
    List<Object> vResult = new ArrayList<Object>();
    Method suiteMethod = null;
    
    // See if we have a suite() method
    try {
      suiteMethod = cls.getMethod("suite", new Class[] {});
    }
    catch(NoSuchMethodException ex) {
      // Do nothing, suiteMethod is already null
    }
    
    //
    // suite() method?
    //
    if (null != suiteMethod) {
      try {
//          ppp("Invoking suite() on " + instance.getClass() + " " + instance);
        //
        // Invoke the suite() method
        //
        Object instance = null;
        if (! Modifier.isStatic(suiteMethod.getModifiers())) {
          instance = cls.newInstance();
        }
        TestSuite ts = (TestSuite) suiteMethod.invoke(instance, new Object[] {});
        // If the returned suite contains a suite method, invoke it recursively
        
        for (int i = 0; i < ts.testCount(); i++) {
          Test test = ts.testAt(i);
          if (test instanceof TestSuite) {
            TestSuite testSuite = (TestSuite) test;
            for (int j = 0; j < testSuite.testCount(); j++) {
              Test subTest = testSuite.testAt(j);
              try {
                Class subTestClass = getTestClass(subTest);
                if (subTestClass != null) {
                  Object[] subObjects = invokeSuite(subTestClass);
                  if (subObjects.length > 0) {
                    vResult.addAll(Arrays.asList(subObjects));
                  }
                  else {
                    vResult.add(subTestClass);
                  }
                }
              }
              catch(Exception ex) {
                // ignore
//                ex.printStackTrace();
              }
            }
          }
          else {
            vResult.add(test.getClass());
          }
        }
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    else {
      vResult.add(cls);
    }
    
    Class[] result = vResult.toArray(new Class[vResult.size()]);
    return result;
  }
  
  private static Class getTestClass(Object test) {
    Class result = null;
    String testName = test.toString();
    try {
      result = Class.forName(testName);
    }
    catch (ClassNotFoundException e) {
      // Try to parse the class name out of the messed up toString()
      // method of the TestCase.  Why won't JUnit make the test class
      // of the TestCase easily accessible?!?
      int l = testName.indexOf("(");
      int r = testName.indexOf(")");
      if (l != -1 && r != -1) {
        String className = testName.substring(l + 1, r);
        try {
          result = Class.forName(className);
        }
        catch(ClassNotFoundException ex) {
          result = null;
        }
      }
    }
    
    return result;
  }
    
  /**
   * @param testClassesFromXml
   */
//  public void _JUnitClassFinder(Class[] classes, XmlTest xmlTest, IAnnotationFinder finder) {
//    {
//      ppp("CREATING CLASS FINDER WITH ");
//      for (Class cls : classes) {
//        ppp("   " + cls);
//      }
//    }
//    
//    for (Class cls : classes) {
////      if (! classExists(cls)) {
//        IClass ic = findOrCreateIClass(cls, null, xmlTest, finder);
//        Utils.log("JUnitClassFinder", 3, "Looking up ClassFinder for " + cls + " " + cls.hashCode() + " " + finder.hashCode());
//        if (null != ic) {
//          Object instance = ic.getInstances(false)[0];
//          putIClass(cls, ic);
//    
//          Method suiteMethod = null;
//          
//          // See if we have a suite() method
//          try {
//            suiteMethod = cls.getMethod("suite", new Class[] {});
//          }
//          catch(NoSuchMethodException ex) {
//            // Do nothing, suiteMethod is already null
//          }
//          
//          //
//          // suite() method?
//          //
//          if (null != suiteMethod) {
//            try {
//      //          ppp("Invoking suite() on " + instance.getClass() + " " + instance);
//              //
//              // Invoke the suite() method
//              //
//              Object suiteInstance = 
//                Utils.createInstance(suiteMethod.getDeclaringClass(), null, xmlTest, finder);
//              assert null != suiteInstance : "Didn't create any instance for class " + suiteMethod.getDeclaringClass();
//              TestSuite ts = (TestSuite) suiteMethod.invoke(suiteInstance, new Object[] {});
//              int n = ts.testCount();
//              
//              //
//              // Pick all the classes of the Object[] returned by suite()
//              //
//              Map<Class, Class> classesReturnedBySuite = new HashMap<Class, Class>(); 
//              for (int i = 0; i < n; i++) {
//                Test test = ts.testAt(i);
//                Class c = test.getClass();
//                // If a TestSuite, we need to iterate until we reach a real
//                // test class
////                if (c instanceof TestSuite) {
////                  
////                }
//                classesReturnedBySuite.put(c, c);
//              }
//              
//              //
//              // Call this finder recursively on them
//              //
//              Class[] otherClasses = 
//                classesReturnedBySuite.values().toArray(new Class[classesReturnedBySuite.size()]);
//              JUnitClassFinder jcf = findClassFinder(cls, otherClasses, xmlTest, finder);
//              
//              //
//              // Add the result of the finder to our current result
//              //
//              IClass[] recursedClasses = jcf.findTestClasses();
//              for (IClass ic2 : recursedClasses) {
//                putIClass(ic2.getRealClass(), ic2);
//              }
//            } 
//            catch (IllegalArgumentException e) {
//              e.printStackTrace();
//            } 
//            catch (IllegalAccessException e) {
//              e.printStackTrace();
//            } 
//            catch (InvocationTargetException e) {
//              e.printStackTrace();
//            }
//          }
////        }
//      } // if ic != null
//    } // for 
//  } 
  
  public JUnitClassFinder findClassFinder(Class currentClass, Class[] others, XmlTest xmlTest, 
      IAnnotationFinder finder) 
  {
    List vOtherClasses = new ArrayList();
    for (Class cls : others) {
      if (! classExists(cls)) {
        vOtherClasses.add(cls);
      }
    }
    Class[] otherClasses = (Class[]) vOtherClasses.toArray(new Class[vOtherClasses.size()]);
    JUnitClassFinder result = new JUnitClassFinder(otherClasses, xmlTest, finder);
    return result;
  }

  private static void ppp(String s) {
    System.out.println("[JUnitClassFinder] " + s);
  }

//  public static void main(String[] args) {
////  Object[] result = JUnitClassFinder.invokeSuite(test.junit.Suite3.class);
//  Object[] result = JUnitClassFinder.invokeSuite(test.junit.MainSuite.class);
//  ppp("" + result.length);
//  }


}
