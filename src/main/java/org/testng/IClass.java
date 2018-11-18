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

  /** If this class implements org.testng.ITest, returns its test name, otherwise returns null. */
  String getTestName();

  /** @return the Java class corresponding to this IClass. */
  Class<?> getRealClass();

  /**
   * Returns all the instances the methods will be invoked upon. This will typically be an array of
   * one object in the absence of a @Factory annotation.
   *
   * @param create flag if a new set of instances must be returned (if set to <tt>false</tt>)
   * @return All the instances the methods will be invoked upon.
   */
  Object[] getInstances(boolean create);

  default Object[] getInstances(boolean create, String errorMsgPrefix) {
    return getInstances(create);
  }

  long[] getInstanceHashCodes();

  void addInstance(Object instance);
}
