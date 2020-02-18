package org.testng;

import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 * <code>IClass</code> represents a test class and a collection of its instances.
 */
public interface IClass {

  /**
   * @return this test class name.  This is the name of the
   * corresponding Java class.
   */
  default String getName() {
    return getXmlClass().getName();
  }

  /**
   * @return the &lt;test&gt; tag this class was found in.
   */
  XmlTest getXmlTest();

  /**
   * @return the *lt;class&gt; tag this class was found in.
   */
  XmlClass getXmlClass();

  default String getNameIndex() {
    return getXmlClass().getNameIndex();
  }

  default int getIndex() {
    return getXmlClass().getIndex();
  }

  /**
   * If this class implements org.testng.ITest, returns its test name, otherwise returns null.
   */
  default String getTestName() {
    return getXmlTest().getName();
  }

  /**
   * @return the Java class corresponding to this IClass.
   */
  default Class<?> getRealClass() {
    return getXmlClass().getSupportClass();
  }

  /**
   * Returns all the instances the methods will be invoked upon.
   * This will typically be an array of one object in the absence
   * of a @Factory annotation.
   *
   * @param create flag if a new set of instances must be returned
   *               (if set to <tt>false</tt>)
   * @return All the instances the methods will be invoked upon.
   */
  Object[] getInstances(boolean create);

  /**
   * @return The number of instances used in this class.  This method
   * is needed for serialization since we don't know ahead of time if the
   * instances of the test classes will be serializable.
   * @deprecated Not used
   */
  @Deprecated
  int getInstanceCount();

  long[] getInstanceHashCodes();

  void addInstance(Object instance);
}
