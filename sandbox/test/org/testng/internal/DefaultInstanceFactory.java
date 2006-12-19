package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.testng.IClass;
import org.testng.TestNGException;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IParameters;
import org.testng.xml.XmlTest;


/**
 * This class/interface 
 */
public class DefaultInstanceFactory implements IInstanceFactory {
  public Object createInstance(
      Class declaringClass, 
      Map<Class, IClass> classes, 
      XmlTest xmlTest, 
      IAnnotationFinder finder) 
  {
    Object result = null;

    try {
      //
      // Any annotated constructor?
      //
      Constructor constructor = findAnnotatedConstructor(finder, declaringClass);
      if (null != constructor) {
        IParameters annotation = (IParameters) finder.findAnnotation(constructor, IParameters.class);

        String[] parameterNames = annotation.getValue();
        Object[] parameters = Parameters.createParameters(constructor,
                                                          "@Parameters",
                                                          parameterNames,
                                                          xmlTest.getParameters(),
                                                          xmlTest.getSuite());
        result = constructor.newInstance(parameters);
      }

      //
      // No, just try to instantiate the parameterless constructor (or the one
      // with a String)
      //
      else {

        // If this class is a (non-static) nested class, the constructor contains a hidden
        // parameter of the type of the enclosing class
        Class[] parameterTypes = new Class[0];
        Object[] parameters = new Object[0];
        Class ec = getEnclosingClass(declaringClass);
        boolean isStatic = 0 != (declaringClass.getModifiers() & Modifier.STATIC);

        // Only add the extra parameter if the nested class is not static
        if ((null != ec) && !isStatic) {
          parameterTypes = new Class[] { ec };

          // Create an instance of the enclosing class so we can instantiate
          // the nested class (actually, we reuse the existing instance).
          IClass enclosingIClass = classes.get(ec);
          Object[] enclosingInstances = null;
          if (null != enclosingIClass) {
            enclosingInstances = enclosingIClass.getInstances(false);
            if ((null == enclosingInstances) || (enclosingInstances.length == 0)) {
              Object o = ec.newInstance();
              enclosingIClass.addInstance(o);
              enclosingInstances = new Object[] { o };
            }
          }
          else {
            enclosingInstances = new Object[] { ec.newInstance() };
          }
          Object enclosingClassInstance = enclosingInstances[0];

          // Utils.createInstance(ec, classes, xmlTest, finder);
          parameters = new Object[] { enclosingClassInstance };
        } // isStatic
        Constructor ct = declaringClass.getDeclaredConstructor(parameterTypes);
        result = ct.newInstance(parameters);
      }
    }
    catch (TestNGException ex) {
      // We need to pass this along
      throw ex;
    }
    catch (InvocationTargetException ex) {
      throw new TestNGException("Cannot instantiate class " + declaringClass.getName(), ex);
    }
    catch (IllegalAccessException ex) {
      result = tryOtherConstructor(declaringClass);
    }
    catch (NoSuchMethodException ex) {
      result = tryOtherConstructor(declaringClass);
    }
    catch (InstantiationException ex) {
      result = tryOtherConstructor(declaringClass);
    }
    catch (Throwable cause) {
      // Something else went wrong when running the constructor
      throw new TestNGException("An error occured while instantiating class " + declaringClass.getName(), cause);
    }

    return result;
  }

  /**
   * Class.getEnclosingClass() only exists on JDK5, so reimplementing it
   * here.
   */
  private static Class getEnclosingClass(Class declaringClass) {
    Class result = null;

    String className = declaringClass.getName();
    int index = className.indexOf("$");
    if (index != -1) {
      String ecn = className.substring(0, index);
      try {
        result = Class.forName(ecn);
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    return result;
  }
  
  private static Object tryOtherConstructor(Class declaringClass) {
    Object result = null;
    try {
      Constructor ctor = declaringClass.getConstructor(new Class[] { String.class });
      result = ctor.newInstance(new Object[] { "Default test name" });
    }
    catch (Exception e) {
      String message = e.getMessage();
      if ((message == null) && (e.getCause() != null)) {
        message = e.getCause().getMessage();
      }
      String error = "Could not create an instance of class " + declaringClass
      + ((message != null) ? (": " + message) : "")
        + ".\nPlease make sure it has a constructor that accepts either a String or no parameter.";
      throw new TestNGException(error);
    }

    return result;
  }
  
  /**
   * Find the best constructor given the parameters found on the annotation
   */
  private static Constructor findAnnotatedConstructor(IAnnotationFinder finder, Class declaringClass) {
    Constructor[] constructors = declaringClass.getDeclaredConstructors();

    for (int i = 0; i < constructors.length; i++) {
      Constructor result = constructors[i];
      IParameters annotation = (IParameters) finder.findAnnotation(result, IParameters.class);

      if (null != annotation) {
        String[] parameters = annotation.getValue();
        Class[] parameterTypes = result.getParameterTypes();
        if (parameters.length != parameterTypes.length) {
          throw new TestNGException("Parameter count mismatch:  " + result + "\naccepts "
                                    + parameterTypes.length
                                    + " parameters but the @Test annotation declares "
                                    + parameters.length);
        }
        else {
          return result;
        }
      }
    }

    return null;
  }
}
