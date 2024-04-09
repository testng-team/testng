package org.testng;

import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/** <code>IClass</code> represents a test class and a collection of its instances. */
public interface IClass {

  /** @return this test class name. This is the name of the corresponding Java class. */
  String getName();

  /** @return the &lt;test&gt; tag this class was found in. */
  XmlTest getXmlTest();

  /** @return the *lt;class&gt; tag this class was found in. */
  XmlClass getXmlClass();

  /** @return its test name if this class implements org.testng.ITest, null otherwise. */
  String getTestName();

  /** @return the Java class corresponding to this IClass. */
  Class<?> getRealClass();

  /**
   * Returns all the instances the methods will be invoked upon. This will typically be an array of
   * one object in the absence of a @Factory annotation.
   *
   * @param create flag if a new set of instances must be returned (if set to <code>false</code>)
   * @return All the instances the methods will be invoked upon.
   * @deprecated - As of TestNG <code>v7.10.0</code>
   */
  @Deprecated
  Object[] getInstances(boolean create);

  /**
   * Returns all the instances the methods will be invoked upon. This will typically be an array of
   * one object in the absence of a @Factory annotation.
   *
   * @param create flag if a new set of instances must be returned (if set to <code>false</code>)
   * @param errorMsgPrefix - Text that should be prefixed to the error message when there are
   *     issues. Can be empty.
   * @return All the instances the methods will be invoked upon.
   * @deprecated - As of TestNG <code>v7.10.0</code>
   */
  @Deprecated
  default Object[] getInstances(boolean create, String errorMsgPrefix) {
    return getInstances(create);
  }

  /** @deprecated - As of TestNG <code>v7.10.0</code> */
  @Deprecated
  long[] getInstanceHashCodes();

  /**
   * @param instance - The instance to be added.
   * @deprecated - As of TestNG <code>v7.10.0</code>
   */
  @Deprecated
  void addInstance(Object instance);
}
