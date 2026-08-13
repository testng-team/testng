package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * A small, immutable key that identifies a method or constructor by name, parameter types and
 * return type. It lets {@link ExecutableCache} de-duplicate members without the (heavier) members
 * having to be their own map keys.
 *
 * <p>Its {@link #equals(Object)}/{@link #hashCode()} match {@link Executable#equals(Object)} once
 * the declaring class is known (the {@link ClassValue} level of the cache), and the return type is
 * part of the key so a covariant method and the compiler-generated bridge that backs it stay
 * distinct.
 *
 * <p>The parameter-type array is never exposed, so the key is immutable once built. Internal to
 * TestNG; not a public API.
 */
public final class MemberKey {

  private final String name;
  private final Class<?>[] parameterTypes;
  private final Class<?> returnType; // methods only; null for a constructor
  private final boolean isConstructor;

  public MemberKey(Executable e) {
    this.name = e.getName();
    // e.getParameterTypes() already hands back a fresh JDK copy, so this array is ours alone.
    this.parameterTypes = e.getParameterTypes();
    if (e instanceof Method) {
      this.isConstructor = false;
      this.returnType = ((Method) e).getReturnType();
    } else if (e instanceof Constructor) {
      this.isConstructor = true;
      this.returnType = null;
    } else {
      // Executable has exactly two JDK subtypes (Method, Constructor); this guards against any
      // exotic one with a clear message instead of a later raw ClassCastException.
      throw new IllegalArgumentException("Unsupported Executable type: " + e.getClass().getName());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MemberKey)) {
      return false;
    }
    MemberKey that = (MemberKey) o;
    return isConstructor == that.isConstructor
        && returnType == that.returnType
        && name.equals(that.name)
        && Arrays.equals(parameterTypes, that.parameterTypes);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + Arrays.hashCode(parameterTypes);
    result = 31 * result + (returnType == null ? 0 : returnType.getName().hashCode());
    result = 31 * result + (isConstructor ? 1 : 0);
    return result;
  }
}
