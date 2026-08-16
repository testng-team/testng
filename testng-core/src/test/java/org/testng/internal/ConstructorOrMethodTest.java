package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.annotations.Test;

/** Unit tests for {@link ConstructorOrMethod}. */
public class ConstructorOrMethodTest {

  @SuppressWarnings("unused")
  static class Sample {
    Sample(String ignored) {}

    void foo(String s) {}

    void foo(int i) {}

    void bar() {}
  }

  // Used only by the "already accessible before wrapping" test, so the shared cache entry for this
  // member is not touched by any other test.
  @SuppressWarnings("unused")
  static class AccessibilitySample {
    private void secret() {}
  }

  private static Method method(String name, Class<?>... params) throws NoSuchMethodException {
    return Sample.class.getDeclaredMethod(name, params);
  }

  @Test
  public void reflectionHandsBackDistinctCopiesSoInterningIsWorthIt() throws NoSuchMethodException {
    // Baseline assumption the whole optimization rests on: the JDK returns a fresh Method copy per
    // lookup, so without interning every wrapper would retain its own heavy handle.
    assertThat(method("bar")).isNotSameAs(method("bar"));
  }

  @Test
  public void interningSharesASingleHandleAcrossWrappers() throws NoSuchMethodException {
    ConstructorOrMethod first = new ConstructorOrMethod(method("foo", String.class));
    ConstructorOrMethod second = new ConstructorOrMethod(method("foo", String.class));

    // Both wrappers converge on the very same interned Method instance...
    assertThat(first.getMethod()).isSameAs(second.getMethod());
    // ...which still represents the method we asked for.
    assertThat(first.getMethod()).isEqualTo(method("foo", String.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void makingOneWrapperAccessibleIsVisibleToASiblingWrapperOfTheSameMember()
      throws NoSuchMethodException {
    // Two wrappers for the same member share one cached handle, so making one accessible is
    // observable through the other. Documents the shared-accessibility semantics of interning.
    ConstructorOrMethod first = new ConstructorOrMethod(method("foo", String.class));
    ConstructorOrMethod second = new ConstructorOrMethod(method("foo", String.class));

    first.makeAccessible();

    assertThat(second.getMethod().isAccessible())
        .as("the sibling wrapper sees the shared handle as accessible")
        .isTrue();
  }

  @Test
  @SuppressWarnings("deprecation")
  public void anAlreadyAccessibleHandleStaysAccessibleThroughTheWrapper()
      throws NoSuchMethodException {
    // juherr's scenario: the member is first interned through a plain lookup, then a copy that was
    // already made accessible is wrapped. Interning must carry that accessibility onto the shared
    // handle instead of silently dropping it.
    new ConstructorOrMethod(AccessibilitySample.class.getDeclaredMethod("secret"));

    Method accessible = AccessibilitySample.class.getDeclaredMethod("secret");
    accessible.setAccessible(true);
    ConstructorOrMethod wrapper = new ConstructorOrMethod(accessible);

    assertThat(wrapper.getMethod().isAccessible())
        .as("an already-accessible incoming handle stays accessible after wrapping")
        .isTrue();
  }

  @Test
  public void overloadsAreNotConflated() throws NoSuchMethodException {
    ConstructorOrMethod withString = new ConstructorOrMethod(method("foo", String.class));
    ConstructorOrMethod withInt = new ConstructorOrMethod(method("foo", int.class));

    assertThat(withString.getMethod()).isNotSameAs(withInt.getMethod());
    assertThat(withString).isNotEqualTo(withInt);
  }

  @Test
  public void equalsAndHashCodePreserveTheExecutableContract() throws NoSuchMethodException {
    ConstructorOrMethod a = new ConstructorOrMethod(method("foo", String.class));
    ConstructorOrMethod b = new ConstructorOrMethod(method("foo", String.class));
    ConstructorOrMethod other = new ConstructorOrMethod(method("bar"));

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
    assertThat(a).isNotEqualTo(other);
  }

  @Test
  public void descriptorAccessorsDoNotRequireResolution() throws NoSuchMethodException {
    ConstructorOrMethod com = new ConstructorOrMethod(method("foo", String.class));

    assertThat(com.getName()).isEqualTo("foo");
    assertThat(com.getDeclaringClass()).isEqualTo(Sample.class);
    assertThat(com.getParameterTypes()).containsExactly(String.class);
    // Defensive copy: mutating the returned array must not corrupt the descriptor.
    com.getParameterTypes()[0] = Integer.class;
    assertThat(com.getParameterTypes()).containsExactly(String.class);
  }

  @Test
  public void wrapsConstructors() throws NoSuchMethodException {
    Constructor<?> ctor = Sample.class.getDeclaredConstructor(String.class);
    ConstructorOrMethod com = new ConstructorOrMethod(ctor);

    assertThat(com.getConstructor()).isEqualTo(ctor);
    assertThat(com.getMethod()).isNull();
    assertThat(com.getName()).isEqualTo(ctor.getName());
  }

  @Test
  public void equalsHashCodeAndDescriptorAgreeAcrossInterningModes() throws NoSuchMethodException {
    // A wrapper built with interning on (shared Entry descriptor) must be indistinguishable from
    // one
    // built with interning off (descriptor read off the strong handle): the two representations
    // have
    // to compare, hash and describe identically.
    ConstructorOrMethod interned = build("true", method("foo", String.class));
    ConstructorOrMethod strong = build("false", method("foo", String.class));

    assertThat(interned).isEqualTo(strong);
    assertThat(strong).isEqualTo(interned);
    assertThat(interned.hashCode()).isEqualTo(strong.hashCode());
    assertThat(strong.getName()).isEqualTo(interned.getName());
    assertThat(strong.getParameterTypes()).containsExactly(interned.getParameterTypes());
    // Overloads must still stay distinct regardless of mode.
    assertThat(build("false", method("foo", int.class))).isNotEqualTo(interned);
  }

  private static ConstructorOrMethod build(String intern, Method m) {
    String saved = System.getProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS);
    System.setProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS, intern);
    try {
      return new ConstructorOrMethod(m);
    } finally {
      if (saved == null) {
        System.clearProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS);
      } else {
        System.setProperty(RuntimeBehavior.INTERN_REFLECTIVE_MEMBERS, saved);
      }
    }
  }

  @Test
  public void killSwitchFallsBackToHoldingThePassedHandleStrongly() throws NoSuchMethodException {
    Method original = method("foo", String.class);
    // With interning disabled the wrapper returns the exact instance it was handed.
    assertThat(build("false", original).getMethod()).isSameAs(original);
  }
}
