package org.testng.internal;

import org.testng.annotations.IDataProviderAnnotation;

import java.lang.reflect.Method;

/**
 * A holder for a pair of Method and IDataProviderAnnotation
 */
public class DataProviderHolder {
  Method method;
  IDataProviderAnnotation annotation;

  public DataProviderHolder(IDataProviderAnnotation annotation, Method method) {
    this.annotation = annotation;
    this.method = method;
  }
}
