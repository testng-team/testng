package org.testng.reporters;

import java.util.Arrays;
import java.util.Objects;
import org.testng.ITestNGMethod;

class MethodInvocationKey {
  private final Object methodIdentity;
  private final Object[] parameters;
  private final int currentInvocationCount;

  MethodInvocationKey(
      ITestNGMethod methodIdentity, Object[] parameters, int currentInvocationCount) {
    this.methodIdentity = methodIdentity;
    this.parameters = parameters;
    this.currentInvocationCount = currentInvocationCount;
  }

  public Object getMethodIdentity() {
    return methodIdentity;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MethodInvocationKey that = (MethodInvocationKey) o;
    return currentInvocationCount == that.currentInvocationCount
        && Objects.equals(methodIdentity, that.methodIdentity)
        && Arrays.equals(parameters, that.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(methodIdentity, Arrays.hashCode(parameters), currentInvocationCount);
  }

  @Override
  public String toString() {
    return "MethodInvocationKey{"
        + "methodIdentity="
        + methodIdentity
        + ", parameters="
        + Arrays.toString(parameters)
        + ", currentInvocationCount="
        + currentInvocationCount
        + '}';
  }
}
