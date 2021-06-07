package org.testng.internal;

import java.util.Arrays;
import java.util.function.Supplier;
import org.testng.ITestNGMethod;

/**
 * This implementation leverages a supplier to lazily load the test methods (data) for the very
 * first time and "remembers it" for later invocations. If the user clears the data (the one that we
 * were remembering) then, it resorts to just using the supplier to provide on-demand evaluation of
 * test methods. This implementation is built such that once its been asked to forget the data, it
 * no longer caches it anymore.
 */
public final class TestMethodContainer implements IContainer<ITestNGMethod> {

  private ITestNGMethod[] methods;
  private final Supplier<ITestNGMethod[]> supplier;
  private boolean isCleared = false;

  public TestMethodContainer(Supplier<ITestNGMethod[]> supplier) {
    this.supplier = supplier;
  }

  public ITestNGMethod[] getItems() {
    if (isCleared()) {
      // If the cached data was cleared, no longer try to refer to it, but instead
      // just resort to on-demand computation.
      return supplier.get();
    }
    if (methods == null) {
      // The first time we are here, methods would be null. So lets lazily initialise it.
      methods = supplier.get();
    }
    return methods;
  }

  @Override
  public boolean isCleared() {
    return isCleared;
  }

  public void clearItems() {
    if (isCleared) {
      return;
    }
    Arrays.fill(methods, null);
    methods = null;
    isCleared = true;
  }
}
