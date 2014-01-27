package org.testng;

import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

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
   * @return the &lt;test&gt; tag this class was found in.
   */
  XmlTest getXmlTest();

  /**
   * @return the *lt;class&gt; tag this class was found in.
   */
  XmlClass getXmlClass();

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
