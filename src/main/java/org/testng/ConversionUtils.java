package org.testng;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper methods used by the Eclipse plug-in when converting tests from JUnit.
 *
 * @author Cedric Beust <cedric@beust.com>
 *
 */
public class ConversionUtils {
  /**
   * Turns the output of a JUnit 4 @Parameters style data provider into
   * one that is suitable for TestNG's @DataProvider.
   */
  public static Object[] wrapDataProvider(Class cls, Collection<Object[]> data) {
    List result = new ArrayList();
    for (Object o : data) {
      Object[] parameters = (Object[]) o;
      Constructor ctor = null;
      try {
        for (Constructor c : cls.getConstructors()) {
          // Just comparing parameter array sizes. Comparing the parameter types
          // is more error prone since we need to take conversions into account
          // (int -> Integer, etc...).
          if (c.getParameterTypes().length == parameters.length) {
            ctor = c;
            break;
          }
        }
        if (ctor == null) {
          throw new TestNGException("Couldn't find a constructor in " + cls);
        }

        result.add(ctor.newInstance(parameters));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return result.toArray();
  }

}
