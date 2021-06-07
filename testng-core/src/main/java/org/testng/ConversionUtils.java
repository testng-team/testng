package org.testng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.testng.internal.objects.InstanceCreator;
import org.testng.log4testng.Logger;

/** Helper methods used by the Eclipse plug-in when converting tests from JUnit. */
@Deprecated
// TODO move code into eclipse project
public class ConversionUtils {
  /**
   * Turns the output of a JUnit 4 @Parameters style data provider into one that is suitable for
   * TestNG's @DataProvider.
   *
   * @param cls The class to create
   * @param data The parameters list
   * @return Collection of class instance
   */
  public static Object[] wrapDataProvider(Class<?> cls, Collection<Object[]> data) {
    List<Object> result = new ArrayList<>();
    for (Object[] parameters : data) {
      try {
        result.add(InstanceCreator.newInstance(cls, parameters));
      } catch (Exception ex) {
        Logger.getLogger(ConversionUtils.class).error(ex.getMessage(), ex);
      }
    }
    return result.toArray();
  }
}
