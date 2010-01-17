package org.testng;

import java.io.Serializable;

/**
 * <code>IClass</code> represents a test class and a collection of its instances.
 * 
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
public interface IClass extends Serializable {
  
  /**
   * @return this test class name.  This is the name of the 
   * corresponding Java class.
   */
  String getName();
  
  /**
   * If this class implements ITest, returns its test name, otherwise returns null.
   */
  String getTestName();

  /**
   * @return the Java class corresponding to this IClass.
   */
  Class getRealClass();

  Object[] getInstances(boolean create);
  
  int getInstanceCount();
  
  long[] getInstanceHashCodes();

  void addInstance(Object instance);
}
