package org.testng.concurrency.samples.parallelization;

import static org.testng.concurrency.samples.parallelization.TestNgRunStateTracker.EventInfo.CLASS_INSTANCE;
import static org.testng.concurrency.samples.parallelization.TestNgRunStateTracker.EventInfo.CLASS_NAME;
import static org.testng.concurrency.samples.parallelization.TestNgRunStateTracker.EventInfo.METHOD_NAME;

import java.util.Objects;

public final class ClassInstanceMethodKey {
  private final String className;
  private final String methodName;
  private final Object classInstance;

  public ClassInstanceMethodKey(TestNgRunStateTracker.EventLog eventLog) {
    className = (String) eventLog.getData(CLASS_NAME);
    methodName = (String) eventLog.getData(METHOD_NAME);
    classInstance = eventLog.getData(CLASS_INSTANCE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClassInstanceMethodKey that = (ClassInstanceMethodKey) o;
    return Objects.equals(className, that.className)
        && Objects.equals(methodName, that.methodName)
        && Objects.equals(classInstance, that.classInstance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(className, methodName, classInstance);
  }
}
