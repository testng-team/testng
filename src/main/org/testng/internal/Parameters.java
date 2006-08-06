package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.TestNGException;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IConfiguration;
import org.testng.internal.annotations.IDataProvider;
import org.testng.internal.annotations.IFactory;
import org.testng.internal.annotations.IParameterizable;
import org.testng.internal.annotations.IParameters;
import org.testng.internal.annotations.ITest;
import org.testng.xml.XmlSuite;

/**
 * Methods that bind parameters declared in testng.xml to actual values
 * used to invoke methods.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * 
 * @@ANNOTATIONS@@
 */
public class Parameters {
  private static final String NULL_VALUE= "null";
  
  public static Object[] createParameters(Constructor ctor, 
                                          String methodAnnotation, 
                                          String[] parameterNames, 
                                          Map<String, String> params,
                                          XmlSuite xmlSuite)
  {
    return createParameters(ctor.toString(), ctor.getParameterTypes(),
        methodAnnotation, parameterNames, params, xmlSuite);
  }

  public static Object[] createTestParameters(Method m, Map<String, String> params, 
      IAnnotationFinder finder, XmlSuite xmlSuite) 
  {
    return createParameters(m, params, finder, xmlSuite, ITest.class, "@Test");
  }
  
  public static Object[] createConfigurationParameters(Method m, Map<String, String> params,
      IAnnotationFinder finder, XmlSuite xmlSuite) 
  {
    return createParameters(m, params, finder, xmlSuite, IConfiguration.class, "@Configuration");
  }

  public static Object[] createFactoryParameters(Method m, Map<String, String> params,
      IAnnotationFinder finder, XmlSuite xmlSuite) 
  {
    return createParameters(m, params, finder, xmlSuite, IFactory.class, "@Factory");
  }
    
  ////////////////////////////////////////////////////////
  
  private static Object[] createParameters(Method m, String methodAnnotation,
      String[] parameterNames, Map<String, String> params, XmlSuite xmlSuite) 
  {
    return createParameters(m.toString(), m.getParameterTypes(),
        methodAnnotation, parameterNames, params, xmlSuite);
  }

  

  /**
   * @param m
   * @param instance
   * @return An array of parameters suitable to invoke this method, possibly
   * picked from the property file
   */
  
  private static Object[] createParameters(String methodName,
                                           Class[] parameterTypes,
                                           String methodAnnotation,
                                           String[] parameterNames,
                                           Map<String, String> params,
                                           XmlSuite xmlSuite)
  {
    Object[] result = new Object[0];
    if(parameterTypes.length > 0) {
      List<Object> vResult = new ArrayList<Object>();
  
      if (parameterNames.length != parameterTypes.length) {
        throw new TestNGException( "Method " + methodName + " needs " 
            + parameterTypes.length + " parameters but " 
            + parameterNames.length
            + " were supplied in the "
            + methodAnnotation
            + " annotation.");
      }
  
      // Assign a value to each parameter
      for(int i = 0; i < parameterNames.length; i++) {
        String p = parameterNames[i];
        String value = params.get(p);
        if (null == value) {
          throw new TestNGException("Parameter '" + p + "' is required by " 
              + methodAnnotation
              + " on method " 
              + methodName
              + "\nbut has not been defined in " + xmlSuite.getFileName());
        }
        
        vResult.add(convertType(parameterTypes[i], value, p));
      }
      
      result = (Object[]) vResult.toArray(new Object[vResult.size()]);
    }
  
    return result;
  }

  private static Object convertType(Class type, String value, String paramName) {
    Object result = null;
    
    if(NULL_VALUE.equals(value.toLowerCase())) {
      if(type.isPrimitive()) {
        Utils.log("Parameters", 2, "Attempt to pass null value to primitive type parameter '" + paramName + "'");
      }
      
      return null; // null value must be used
    }
    
    if(type == String.class) {
      result = value;
    } 
    else if(type == int.class || type == Integer.class) {
      result = new Integer(Integer.parseInt(value));
    }
    else if(type == boolean.class || type == Boolean.class) {
      result = new Boolean(value);          
    }
    else if(type == byte.class || type == Byte.class) {
      result = new Byte(Byte.parseByte(value));                    
    }
    else if(type == char.class || type == Character.class) {
      result = new Character(value.charAt(0));                              
    }
    else if(type == double.class || type == Double.class) {
      result = new Double(Double.parseDouble(value));
    }
    else if(type == float.class || type == Float.class) {
      result = new Float(Float.parseFloat(value));          
    }
    else if(type == long.class || type == Long.class) {
      result = new Long(Long.parseLong(value));
    }
    else if(type == short.class || type == Short.class) {
      result = new Short(Short.parseShort(value));
    }
    else {
      assert false : "Unsupported type parameter : " + type;
    }
    
    return result;
  }
  
  public static Method findDataProvider(Class clazz, Method m, IAnnotationFinder finder) {
    Method result = null;
    ITest annotation = AnnotationHelper.findTest(finder, m);
    if (null != annotation) {
      String dataProviderName = annotation.getDataProvider();
      if (null != dataProviderName && ! "".equals(dataProviderName)) {
        result = findDataProvider(clazz, finder, dataProviderName);
        if (null != result) {
          return result;
        }
      }
    }
    
    return null;
  }
  
  /**
   * @return the values for Parameters, if any was supplied in a @DataProvider
   * (@Parameters (values = { "a", "b" }))
   */
  /*public static Iterator<Object[]> getParameterValues(Method m, Map<String, String> params,
    IAnnotationFinder finder)
  {
//    return new String[][] {
//        new String[] { "Cedric", "Beust" },
//        new String[] { "Anne Marie", "Condie" },
//    };
    Iterator<Object[]> result = null;
    //
    // Try to find an @Parameters annotation
    //
    Method dataProvider = findDataProvider(m, finder);
    if (null != dataProvider) {
      // NOTE(cbeust)
      // Should reuse an existing instance?
      Utils.log("", 3, "Invoking DataProvider " + dataProvider + " for test method " + m);
      result = MethodHelper.invokeDataProvider(dataProvider);
    }
    
    return result;
  }*/
  
  /**
   * Find a method that has a @DataProvider(name=name)
   */
  private static Method findDataProvider(Class cls, IAnnotationFinder finder, String name) {
    for (Method m : ClassHelper.getAvailableMethods(cls)) {
      IDataProvider dp = (IDataProvider) finder.findAnnotation(m, IDataProvider.class);
      if (null != dp && dp.getName().equals(name)) {
        return m;
      }
    }
    
    return null;
  }

  public static Object[] createParameters(Method m, Map<String, String> params,
      IAnnotationFinder finder, XmlSuite xmlSuite, Class annotationClass, String atName) 
  {
    Object[] result = new Object[0];
    //
    // Try to find an @Parameters annotation
    //
    IParameters annotation = (IParameters) finder.findAnnotation(m, IParameters.class);
    if(null != annotation) {
      String[] parameterNames = annotation.getValue();
      result = createParameters(m, atName, parameterNames, params, xmlSuite);
    }  
    
    //
    // Else, use the deprecated syntax
    //
    else {    
      IParameterizable a = (IParameterizable) finder.findAnnotation(m, annotationClass);
      if(null != a) {
        String[] parameterNames = a.getParameters();
        result = createParameters(m, "@Configuration", parameterNames, params, xmlSuite);
      }
    }
    
    return result;
  }
}
