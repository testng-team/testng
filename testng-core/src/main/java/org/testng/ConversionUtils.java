package org.testng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.testng.internal.objects.InstanceCreator;
import org.testng.log4testng.Logger;

/**
 * Helper methods for converting JUnit parameter providers to TestNG instances.
 *
 * @deprecated Kept for callers of the legacy JUnit conversion API.
 */
@Deprecated
public class ConversionUtils {
  /**
   * Creates an instance for each parameter row.
   *
   * <p>Logs failed rows and omits them from the result.
   *
   * @param cls the class to create
   * @param data the parameter rows
   * @return the created instances
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
