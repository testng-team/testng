package org.testng;

import java.io.Serializable;

/**
 * <code>IClass</code> represents a test class and a collection of its instances.
 * 
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
public interface IClass extends Serializable {
  
  /**
   * Returns this test class' name. This is the name of the corresponding Java class.
   * @return this test class' name.
   */
  String getName();

  /**
   * Returns the Java class corresponding to this IClass.
   * @return the Java class corresponding to this IClass.
   */
  Class getRealClass();

  /**
   * TODO cquezel JavaDoc.
   *
   * @param create
   * @return
   */
  Object[] getInstances(boolean create);
  
  /**
   * TODO cquezel JavaDoc.
   *
   * @return
   */
  int getInstanceCount();
  
  /**
   * TODO cquezel JavaDoc.
   *
   * @return
   */
  long[] getInstanceHashCodes();

  /**
   * TODO cquezel JavaDoc.
   *
   * @param instance
   */
  void addInstance(Object instance);
}
