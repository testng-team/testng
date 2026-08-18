package org.testng.internal;

import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.IDataProviderAnnotation;

/** Represents an @{@link org.testng.annotations.DataProvider} annotated method. */
class DataProviderMethodRemovable extends DataProviderMethod {

  DataProviderMethodRemovable(Object instance, Method method, IDataProviderAnnotation annotation) {
    super(instance, method, annotation);
  }

  public void setInstance(@Nullable Object instance) {
    this.instance = instance;
  }

  public void setMethod(@Nullable Method method) {
    this.method = method;
  }
}
