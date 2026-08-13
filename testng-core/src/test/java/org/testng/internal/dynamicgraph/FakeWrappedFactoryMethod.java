package org.testng.internal.dynamicgraph;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import org.testng.ITestNGMethod;
import org.testng.internal.WrappedTestNGMethod;

public class FakeWrappedFactoryMethod extends WrappedTestNGMethod {

  // Assigns a stable id per distinct instance object, so that (as in production) every method bound
  // to the same factory instance shares one instance id while different instances get different
  // ones. Without this, instance-based grouping/ordering cannot tell the instances apart.
  private static final Map<Object, UUID> INSTANCE_IDS =
      Collections.synchronizedMap(new IdentityHashMap<>());

  private final Object instance;

  public FakeWrappedFactoryMethod(ITestNGMethod testNGMethod, Object instance) {
    super(testNGMethod);
    this.instance = instance;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public UUID getInstanceId() {
    return INSTANCE_IDS.computeIfAbsent(instance, key -> UUID.randomUUID());
  }
}
