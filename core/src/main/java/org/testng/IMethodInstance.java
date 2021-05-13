package org.testng;

/** This interface captures a test method along with all the instances it should be run on. */
public interface IMethodInstance {

  ITestNGMethod getMethod();

  Object getInstance();
}
