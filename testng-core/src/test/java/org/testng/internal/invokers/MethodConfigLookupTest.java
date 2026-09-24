package org.testng.internal.invokers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.ITestClassConfigInfo;
import org.testng.internal.NoOpTestClass;
import org.testng.internal.WrappedTestNGMethod;

/**
 * The invoker must read the per-instance index when the test class overrides it, and must keep the
 * flat scan otherwise. An empty default is not proof that the instance has no configurations.
 */
public class MethodConfigLookupTest {

  private static final BiPredicateAlways ALWAYS = new BiPredicateAlways();

  @Test
  public void anOverriddenIndexIsWhatTheInvokerReads() {
    Object instance = new Object();
    UUID instanceId = UUID.randomUUID();
    ITestNGMethod indexed = methodOn(instance);
    ITestNGMethod flat = methodOn(instance);
    IndexingConfigInfo testClass = new IndexingConfigInfo(indexed, flat);

    assertThat(TestNgMethodUtils.filterBeforeTestMethods(instance, testClass, ALWAYS, instanceId))
        .containsExactly(indexed);
    // After is not overridden, so the empty default must not hide the flat list.
    assertThat(TestNgMethodUtils.filterAfterTestMethods(instance, testClass, ALWAYS, instanceId))
        .containsExactly(flat);
  }

  @Test
  public void aConfigInfoThatDoesNotOverrideKeepsTheFlatScan() {
    Object instance = new Object();
    UUID instanceId = UUID.randomUUID();
    ITestNGMethod flatBefore = methodOn(instance);
    ITestNGMethod flatAfter = methodOn(instance);
    ConfigInfoWithoutMethodIndex testClass =
        new ConfigInfoWithoutMethodIndex(flatBefore, flatAfter);

    assertThat(TestNgMethodUtils.filterBeforeTestMethods(instance, testClass, ALWAYS, instanceId))
        .containsExactly(flatBefore);
    assertThat(TestNgMethodUtils.filterAfterTestMethods(instance, testClass, ALWAYS, instanceId))
        .containsExactly(flatAfter);
  }

  @Test
  public void aWrappedNonBaseMethodDoesNotUseItsGeneratedId() {
    ITestNGMethod delegate = mock(ITestNGMethod.class);
    WrappedTestNGMethod wrapped = new WrappedTestNGMethod(delegate);

    assertThat(wrapped.getInstanceId()).isNotNull();
    assertThat(wrapped.hasDelegatedInstanceId()).isFalse();
    assertThat(TestNgMethodUtils.configInstanceId(wrapped)).isNull();
  }

  @Test
  public void aWrappedBaseMethodKeepsTheDelegatedId() {
    UUID instanceId = UUID.randomUUID();
    BaseTestMethod delegate = mock(BaseTestMethod.class);
    when(delegate.getInstanceId()).thenReturn(instanceId);
    WrappedTestNGMethod wrapped = new WrappedTestNGMethod(delegate);

    assertThat(wrapped.hasDelegatedInstanceId()).isTrue();
    assertThat(TestNgMethodUtils.configInstanceId(wrapped)).isEqualTo(instanceId);
  }

  private static ITestNGMethod methodOn(Object instance) {
    ITestNGMethod method = mock(ITestNGMethod.class);
    when(method.getInstance()).thenReturn(instance);
    return method;
  }

  private static final class BiPredicateAlways
      implements java.util.function.BiPredicate<ITestNGMethod, org.testng.IClass> {
    @Override
    public boolean test(ITestNGMethod method, org.testng.IClass testClass) {
      return true;
    }
  }

  /** Implements the class-level index only. The new method defaults stay empty. */
  private static class ConfigInfoWithoutMethodIndex extends NoOpTestClass
      implements ITestClassConfigInfo {

    ConfigInfoWithoutMethodIndex(ITestNGMethod before, ITestNGMethod after) {
      setTestClass(ConfigInfoWithoutMethodIndex.class);
      setBeforeTestMethods(new ITestNGMethod[] {before});
      setAfterTestMethod(new ITestNGMethod[] {after});
    }

    @Override
    public List<ITestNGMethod> getAllBeforeClassMethods() {
      return List.of();
    }

    @Override
    public List<ITestNGMethod> getAllAfterClassMethods() {
      return List.of();
    }

    @Override
    public List<ITestNGMethod> getInstanceBeforeClassMethods(@Nullable UUID instanceId) {
      return List.of();
    }

    @Override
    public List<ITestNGMethod> getInstanceAfterClassMethods(@Nullable UUID instanceId) {
      return List.of();
    }
  }

  /** Overrides only the before-method index, so after must still fall back. */
  private static class IndexingConfigInfo extends ConfigInfoWithoutMethodIndex {

    private final ITestNGMethod indexedBefore;

    IndexingConfigInfo(ITestNGMethod indexedBefore, ITestNGMethod flatAfter) {
      super(mock(ITestNGMethod.class), flatAfter);
      this.indexedBefore = indexedBefore;
    }

    @Override
    public List<ITestNGMethod> getInstanceBeforeTestMethods(@Nullable UUID instanceId) {
      return List.of(indexedBefore);
    }
  }
}
