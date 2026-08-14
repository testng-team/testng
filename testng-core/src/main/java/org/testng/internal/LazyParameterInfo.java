package org.testng.internal;

import java.util.function.Supplier;

/**
 * A lazy, memoizing {@link IParameterInfo} used by constructor based {@code @Factory} methods when
 * lazy instantiation is enabled. The backing test-class instance is not created up-front (during
 * test collection); instead it is instantiated on the first call to {@link #getInstance()} — which
 * happens on the worker thread, right before the instance's first configuration/test method runs —
 * and memoized for every subsequent access.
 *
 * <p>The target class, index and parameters are all known without instantiating the instance, so
 * setup-time code paths (class discovery, method binding, dependency resolution) can operate purely
 * on this metadata and leave construction for run time.
 */
public class LazyParameterInfo implements IParameterInfo {

  private final int index;
  private final Object[] parameters;
  private final int currentIndex;
  private final Class<?> targetClass;
  private final Supplier<Object> creator;

  private final Object lock = new Object();
  private volatile boolean instantiated = false;
  private volatile Object instance;
  private volatile Throwable failure;

  public LazyParameterInfo(
      int index,
      Object[] parameters,
      int currentIndex,
      Class<?> targetClass,
      Supplier<Object> creator) {
    this.index = index;
    this.parameters = parameters;
    this.currentIndex = currentIndex;
    this.targetClass = targetClass;
    this.creator = creator;
  }

  @Override
  public Object getInstance() {
    if (!instantiated) {
      synchronized (lock) {
        if (!instantiated) {
          try {
            Object created = creator.get();
            if (created == null) {
              throw new IllegalStateException("Factory instance creator returned null");
            }
            instance = created;
          } catch (Throwable t) {
            failure = t;
          } finally {
            instantiated = true;
          }
        }
      }
    }
    // A construction failure is memoized and surfaced through getInstantiationFailure() rather
    // than thrown here: throwing on every access would make it impossible to build the (localized)
    // skip result for this instance without re-triggering the failure. On failure this simply
    // reports "no instance".
    return instance;
  }

  @Override
  public Throwable getInstantiationFailure() {
    return failure;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public int getInvocationIndex() {
    return currentIndex;
  }

  @Override
  public Object[] getParameters() {
    return parameters;
  }

  @Override
  public Class<?> getTargetClass() {
    return targetClass;
  }

  @Override
  public boolean isLazilyInitialized() {
    return true;
  }

  @Override
  public boolean isInstanceInstantiated() {
    return instantiated && failure == null;
  }
}
