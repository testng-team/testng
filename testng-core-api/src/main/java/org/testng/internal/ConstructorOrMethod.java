package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;

/**
 * Wraps either a method or a constructor.
 *
 * <p>In a big suite the same few methods get wrapped again and again — once per {@code @Factory}
 * instance and per clone — so a plain wrapper would keep a separate reflective handle for each. To
 * avoid that, this wrapper de-duplicates through the shared {@link ExecutableCache}: every wrapper
 * for the same member points at one {@code Method}/{@code Constructor}.
 *
 * <p>Set {@link RuntimeBehavior#internReflectiveMembers()} to {@code false} to turn the sharing off
 * and simply hold the handle you were given, exactly like older TestNG. Either way the behaviour
 * visible to callers is identical.
 */
public class ConstructorOrMethod {

  // The wrapped handle: a shared, de-duplicated one when interning is on, or the exact one we were
  // given when it is off.
  private final Executable member;

  private boolean m_enabled = true;

  public ConstructorOrMethod(Executable e) {
    this.member = RuntimeBehavior.internReflectiveMembers() ? ExecutableCache.DEFAULT.intern(e) : e;
  }

  public Class<?> getDeclaringClass() {
    return member.getDeclaringClass();
  }

  public String getName() {
    return member.getName();
  }

  public Class<?>[] getParameterTypes() {
    return member.getParameterTypes(); // the JDK returns a fresh copy each call
  }

  /**
   * @return the wrapped member if it is a method, or {@code null} if it is a constructor. Prefer
   *     {@link #requireMethod()} unless the null is what you are testing for.
   */
  public @Nullable Method getMethod() {
    return member instanceof Method ? (Method) member : null;
  }

  /**
   * @return the wrapped member if it is a constructor, or {@code null} if it is a method. Prefer
   *     {@link #requireConstructor()} unless the null is what you are testing for.
   */
  public @Nullable Constructor<?> getConstructor() {
    return member instanceof Constructor ? (Constructor<?>) member : null;
  }

  /**
   * The wrapped member as a {@link Method}, for the callers that only ever see a test or a
   * configuration method.
   *
   * @return the wrapped method
   * @throws NullPointerException if this wrapper holds a constructor -- the same failure the call
   *     sites saw before, with a message instead of a bare dereference
   */
  public Method requireMethod() {
    if (member instanceof Method) {
      return (Method) member;
    }
    throw new NullPointerException("Expected a method, but " + member + " is a constructor");
  }

  /**
   * The wrapped member as a {@link Constructor}, for the callers that have already established it
   * is not a method.
   *
   * @return the wrapped constructor
   * @throws NullPointerException if this wrapper holds a method -- the same failure the call sites
   *     saw before, with a message instead of a bare dereference
   */
  public Constructor<?> requireConstructor() {
    if (member instanceof Constructor) {
      return (Constructor<?>) member;
    }
    throw new NullPointerException("Expected a constructor, but " + member + " is a method");
  }

  /**
   * Makes the wrapped member accessible. When interning is on the handle is shared, so this is
   * observable through every wrapper of the same member.
   */
  public void makeAccessible() {
    member.setAccessible(true);
  }

  @Override
  // getClass() on purpose: this is a public, non-final type of the published API, and instanceof
  // would make a user's subclass equal to its base while the base is never equal to it. Sealing it
  // instead would break every user who extends it.
  @SuppressWarnings("EqualsGetClass")
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    // Executable#equals is value-based, so this holds whether or not the two sides share one
    // handle.
    return member.equals(((ConstructorOrMethod) o).member);
  }

  @Override
  public int hashCode() {
    // Objects.hash(executable) — the exact value older TestNG produced, so the iteration order of
    // any
    // HashSet/HashMap holding these (e.g. FailedReporter's re-run set) is unchanged.
    return 31 + member.hashCode();
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  public boolean getEnabled() {
    return m_enabled;
  }

  @Override
  public String toString() {
    return member.toString();
  }

  public String stringifyParameterTypes() {
    return Utils.stringifyTypes(getParameterTypes());
  }
}
