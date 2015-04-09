package org.testng.internal;

import org.testng.annotations.IDataProviderAnnotation;

import java.lang.reflect.Method;

/**
 * A holder for a pair of Method and IDataProviderAnnotation
 */
public class DataProviderHolder {
  Object instance;
  Method method;
  IDataProviderAnnotation annotation;

  public DataProviderHolder(IDataProviderAnnotation annotation, Method method, Object instance) {
    this.annotation = annotation;
    this.method = method;
    this.instance = instance;
  }
}
