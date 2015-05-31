package org.testng.internal.collections;

import org.testng.collections.Objects;





public class Pair<A, B> {
  private final A first;
  private final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A first() {
    return first;
  }

  public B second() {
    return second;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Pair other = (Pair) obj;
    if (first == null) {
      if (other.first != null) {
        return false;
      }
    }
    else if (!first.equals(other.first)) {
      return false;
    }
    if (second == null) {
      if (other.second != null) {
        return false;
      }
    }
    else if (!second.equals(other.second)) {
      return false;
    }
    return true;
  }

  public static <A, B> Pair<A, B> create(A first, B second) {
    return of(first, second);
  }

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<>(a, b);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
        .add("first", first())
        .add("second", second())
        .toString();
  }
}