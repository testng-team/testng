package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class KeyAwareAutoCloseableLockTest {

  @Test
  public void ensureLockIsReEntrant() {
    String key = "TestNG";
    KeyAwareAutoCloseableLock lock = new KeyAwareAutoCloseableLock();
    KeyAwareAutoCloseableLock.AutoReleasable outer;
    KeyAwareAutoCloseableLock.AutoReleasable inner1;
    KeyAwareAutoCloseableLock.AutoReleasable inner2;
    try (KeyAwareAutoCloseableLock.AutoReleasable ignore = lock.lockForObject(key)) {
      outer = ignore;
      try (KeyAwareAutoCloseableLock.AutoReleasable ignore1 = lock.lockForObject(key)) {
        inner1 = ignore1;
      }
      try (KeyAwareAutoCloseableLock.AutoReleasable ignore2 = lock.lockForObject(key)) {
        inner2 = ignore2;
      }
    }
    assertThat(outer).isEqualTo(inner1);
    assertThat(inner1).isEqualTo(inner2);
  }
}
