package org.testng.internal;


import org.testng.IClass;
import org.testng.IInstanceInfo;
import org.testng.IObjectFactory;
import org.testng.ITestContext;
import org.testng.TestNGException;
import org.testng.annotations.IAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class creates an ITestClass from a test class.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestNGClassFinder extends BaseClassFinder {
  private ITestContext m_testContext = null;
  private Map<Class, List<Object>> m_instanceMap = Maps.newHashMap();

  public TestNGClassFinder(Class[] classes,
                           Map<Class, List<Object>> instanceMap,
                           XmlTest xmlTest,
                           IAnnotationFinder annotationFinder,
                           ITestContext testContext)
  {
    m_testContext = testContext;

    if(null == instanceMap) {
      instanceMap= Maps.newHashMap();
    }

    //
    // Find all the new classes and their corresponding instances
    //
    Class[] allClasses= classes;

    IObjectFactory objectFactory = testContext.getSuite().getObjectFactory();
    //very first pass is to find ObjectFactory, can't create anything else until then
    if(objectFactory == null) {
      objectFactory = new ObjectFactoryImpl();
      outer:
      for (Class cls : allClasses)
        try {
          if (null != cls) {
            for (Method m : cls.getMethods()) {
              IAnnotation a = annotationFinder.findAnnotation(m, org.testng.annotations.IObjectFactoryAnnotation.class);
              if (null != a) {
                if (!IObjectFactory.class.isAssignableFrom(m.getReturnType())) {
                  throw new TestNGException("Return type of " + m + " is not IObjectFactory");
                }
                try {
                  Object instance = cls.newInstance();
                  if (m.getParameterTypes().length > 0 && m.getParameterTypes()[0].equals(ITestContext.class)) {
                    objectFactory = (IObjectFactory) m.invoke(instance, testContext);
                  } else {
                    objectFactory = (IObjectFactory) m.invoke(instance);
                  }
                  break outer;
                }
                catch (Exception ex) {
                  throw new TestNGException("Error creating object factory", ex);
                }
              }
            }
          }
        } catch (NoClassDefFoundError e) {
          Utils.log("[TestNGClassFinder]", 1, "Unable to read methods on class " + cls.getName() + " - unable to resolve class reference " + e.getMessage());

          for (Iterator<XmlClass> iterator = xmlTest.getXmlClasses().iterator(); iterator.hasNext();) {
            XmlClass xmlClass = iterator.next();

            if (xmlClass.getDeclaredClass() == Boolean.TRUE && xmlClass.getName().equals(cls.getName())) {
              throw e;
            }
          }

        }
    }

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
          Utils.log("", 5, "[WARN] Found an abstract class with no valid instance attached: " + cls);
          continue;
        }

        IClass ic= findOrCreateIClass(cls, thisInstance, xmlTest, annotationFinder,
                                      objectFactory);
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
              annotationFinder,
              m_testContext);
            List<Class> moreClasses= Lists.newArrayList();

            {
//            ppp("INVOKING FACTORY " + fm + " " + this.hashCode());
              Object[] instances= fm.invoke();

              //
              // If the factory returned IInstanceInfo, get the class from it,
              // otherwise, just call getClass() on the returned instances
              //
              if (instances.length > 0) {
                Class elementClass = instances[0].getClass();
                if(IInstanceInfo.class.isAssignableFrom(elementClass)) {
                  for(Object o : instances) {
                    IInstanceInfo ii = (IInstanceInfo) o;
                    addInstance(ii.getInstanceClass(), ii.getInstance());
                    moreClasses.add(ii.getInstanceClass());
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
            }

            if(moreClasses.size() > 0) {
              TestNGClassFinder finder=
                new TestNGClassFinder(moreClasses.toArray(
                    new Class[moreClasses.size()]),
                    m_instanceMap,
                    xmlTest,
                    annotationFinder,
                    m_testContext);

              IClass[] moreIClasses= finder.findTestClasses();
              for(IClass ic2 : moreIClasses) {
                putIClass(ic2.getRealClass(), ic2);
              }
            } // if moreClasses.size() > 0
          }
        } // null != ic
      } // if not TestNG class
      else {
        Utils.log("TestNGClassFinder", 3, "SKIPPING CLASS " + cls + " no TestNG annotations found");
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

      try {
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
      } catch (NoClassDefFoundError e) {
        Utils.log("[TestNGClassFinder]", 1, "Unable to read methods on class " + cls.getName() + " - unable to resolve class reference " + e.getMessage());
        return false;
      }
  }

  private void addInstance(Class clazz, Object o) {
    List<Object> list= m_instanceMap.get(clazz);

    if(null == list) {
      list= Lists.newArrayList();
      m_instanceMap.put(clazz, list);
    }

    list.add(o);
  }

  public static void ppp(String s) {
    System.out.println("[TestNGClassFinder] " + s);
  }

}
