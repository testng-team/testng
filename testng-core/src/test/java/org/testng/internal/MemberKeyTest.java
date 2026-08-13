package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.annotations.Test;

/** Unit tests for {@link MemberKey}. */
public class MemberKeyTest {

  @SuppressWarnings("unused")
  static class Sample {
    void foo(String s) {}
  }

  interface Parent<T> {
    T value();
  }

  // A covariant override: the compiler adds a bridge method `Object value()` next to the real
  // `String value()`, so this class has two `value()` methods that differ only in return type.
  static class Child implements Parent<String> {
    @Override
    public String value() {
      return "child";
    }
  }

  @Test
  public void equalMembersProduceEqualKeys() throws NoSuchMethodException {
    MemberKey a = new MemberKey(Sample.class.getDeclaredMethod("foo", String.class));
    MemberKey b = new MemberKey(Sample.class.getDeclaredMethod("foo", String.class));

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  public void aCovariantMethodAndItsBridgeGetDistinctKeys() throws NoSuchMethodException {
    Method real = Child.class.getDeclaredMethod("value"); // most specific: returns String
    Method bridge = null;
    for (Method m : Child.class.getDeclaredMethods()) {
      if (m.getName().equals("value") && m.isBridge()) {
        bridge = m;
      }
    }
    assertThat(bridge).as("the compiler should have generated a bridge method").isNotNull();

    // Same name and parameters, different return type: the keys must differ, or the cache would
    // collapse the real method and its bridge into one entry.
    assertThat(new MemberKey(real)).isNotEqualTo(new MemberKey(bridge));
  }
}
