package org.testng.internal;

import java.lang.reflect.Method;
import org.testng.annotations.IDataProviderAnnotation;

/** Represents an @{@link org.testng.annotations.DataProvider} annotated method. */
class DataProviderMethodRemovable extends DataProviderMethod {

  DataProviderMethodRemovable(Object instance, Method method, IDataProviderAnnotation annotation) {
    super(instance, method, annotation);
  }

  public void setInstance(Object instance) {
    this.instance = instance;
  }

  public void setMethod(Method method) {
    this.method = method;
  }
}
