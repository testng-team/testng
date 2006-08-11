package org.testng.internal;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.IClass;
import org.testng.IInstanceInfo;
import org.testng.ITestClass;
import org.testng.ITestMethodFinder;
import org.testng.TestClass;
import org.testng.TestRunner;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/**
 * This class creates an ITestClass from a test class.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestNGClassFinder extends BaseClassFinder {

  private Map<Class, List<Object>> m_instanceMap= new HashMap<Class, List<Object>>();

  public TestNGClassFinder(Class[] classes,
                           Map<Class, List<Object>> instanceMap,
                           XmlTest xmlTest,
                           IAnnotationFinder annotationFinder) 
  {
    if(null == instanceMap) {
      instanceMap= new HashMap<Class, List<Object>>();
    }

    //
    // Find all the new classes and their corresponding instances
    //
    Class[] allClasses= classes;

    for(Class cls : allClasses) {
      if((null == cls)) {
        ppp("FOUND NULL CLASS IN FOLLOWING ARRAY:");
        int i= 0;
        for(Class c : allClasses) {
          ppp("  " + i + ": " + c);
        }
        
        continue;
      }

      if(isTestNGClass(cls, annotationFinder)) {
        List allInstances= instanceMap.get(cls);
        Object thisInstance= (null != allInstances) ? allInstances.get(0) : null;
        
        // If annotation class and instances are abstract, skip them
        if ((null == thisInstance) && Modifier.isAbstract(cls.getModifiers())) {
          Utils.log("", 2, "[WARN] Found an abstract class with no valid instance attached: " + cls);
          continue;
        }
        
        IClass ic= findOrCreateIClass(cls, thisInstance, xmlTest, annotationFinder);
        if(null != ic) {
          Object[] theseInstances = ic.getInstances(false);
          if (theseInstances.length == 0) {
            theseInstances = ic.getInstances(true);
          }
          Object instance= theseInstances[0];
          putIClass(cls, ic);

          Method factoryMethod= ClassHelper.findFactoryMethod(cls, annotationFinder);
          if(null != factoryMethod) {
            FactoryMethod fm= new FactoryMethod( /* cls, */
              factoryMethod,
              instance,
              xmlTest,
              annotationFinder);
            List<Class> moreClasses= new ArrayList<Class>();

            {
//            ppp("INVOKING FACTORY " + fm + " " + this.hashCode());
              Object[] instances= fm.invoke();

              //
              // If the factory returned IInstanceInfo, get the class from it,
              // otherwise, just call getClass() on the returned instances
              //
              Class elementClass= instances.getClass().getComponentType();
              if(IInstanceInfo.class.isAssignableFrom(elementClass)) {
                IInstanceInfo[] infos= (IInstanceInfo[]) instances;
                for(IInstanceInfo o : infos) {
                  addInstance(o.getInstanceClass(), o.getInstance());
                  moreClasses.add(o.getInstanceClass());
                }
              }
              else {
                for(Object o : instances) {
                  addInstance(o.getClass(), o);
                  if(!classExists(o.getClass())) {
                    moreClasses.add(o.getClass());
                  }
                }
              }
            }

            if(moreClasses.size() > 0) {
              TestNGClassFinder finder= new TestNGClassFinder(moreClasses.toArray(new Class[moreClasses
                                                                                  .size()]),
                                                              m_instanceMap,
                                                              xmlTest,
                                                              annotationFinder);

              IClass[] moreIClasses= finder.findTestClasses();
              for(IClass ic2 : moreIClasses) {
                putIClass(ic2.getRealClass(), ic2);
              }
            } // if moreClasses.size() > 0
          }
        } // null != ic
      } // if cls is abstract
      else {
        Utils.log("TestNGClassFinder", 2, "SKIPPING CLASS " + cls);
      }
    } // for

    //
    // Add all the instances we found to their respective IClasses
    //
    for(Class c : m_instanceMap.keySet()) {
      List<Object> instances= m_instanceMap.get(c);
      for(Object instance : instances) {
        IClass ic= getIClass(c);
        if(null != ic) {
          ic.addInstance(instance);
        }
      }
    }
  }
  
  /**
   * Checks if class is a testng class based on the {@link IAnnotationFinder} 
   * passed in, which may be a jdk14 or jdk15 {@link IAnnotationFinder} instance.
   * @param cls The class being tested
   * @param annotationFinder The instance of annotation finder being used
   * @return True if class has any testng annotations
   */
  public static boolean isTestNGClass(Class cls, IAnnotationFinder annotationFinder) {
	  Class[] allAnnotations= AnnotationHelper.getAllAnnotations();
	  
	    for(Class annotation : allAnnotations) {
          // Try on the methods
          for(Method m : cls.getMethods()) {
            IAnnotation ma= annotationFinder.findAnnotation(m, annotation);
            if(null != ma) {
              return true;
            }
          }

	      // Try on the class
	      IAnnotation a= annotationFinder.findAnnotation(cls, annotation);
	      if(null != a) {
	        return true;
	      }

	      // Try on the constructors
	      for(Constructor ctor : cls.getConstructors()) {
	        IAnnotation ca= annotationFinder.findAnnotation(ctor, annotation);
	        if(null != ca) {
	          return true;
	        }
	      }
	    }

	    return false;
  }
  
  private void addInstance(Class clazz, Object o) {
    List<Object> list= m_instanceMap.get(clazz);

    if(null == list) {
      list= new ArrayList<Object>();
      m_instanceMap.put(clazz, list);
    }

    list.add(o);
  }

  public static void ppp(String s) {
    System.out.println("[TestNGClassFinder] " + s);
  }

}
