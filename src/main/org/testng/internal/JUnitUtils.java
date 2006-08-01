package org.testng.internal;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Help methods for JUnit
 * 
 * @author cbeust
 * @date Jan 14, 2006
 */
public class JUnitUtils {
  
  public static boolean isJUnitClass(Class cls) {
    return isAssignableFromTestCase(cls) || isAssignableFromTest(cls);
  }
  
  public static boolean isAssignableFromTestCase(Class cls) {
    boolean result = false;
    
    try {
      result = TestCase.class.isAssignableFrom(cls);
    }
    catch (NoClassDefFoundError ex) {
      // couldn't find JUnit classes, do nothing
    }
    
    return result;    
  }
  
  public static boolean isAssignableFromTest(Class cls) {
    boolean result = false;
    
    try {
      result = Test.class.isAssignableFrom(cls);
    }
    catch (NoClassDefFoundError ex) {
      // couldn't find JUnit classes, do nothing
    }
    
    return result;    
  }

}
