package org.testng.conffailure.samples.issue3627;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A test class whose instances all equal each other, so that only identity tells two instances
 * apart. The {@code @BeforeMethod} method of each instance fails or skips with an exception that
 * names the instance.
 */
public abstract class EqualInstancesSample {

  private final String name;
  private final boolean skip;

  protected EqualInstancesSample(String name, boolean skip) {
    this.name = name;
    this.skip = skip;
  }

  // A @BeforeMethod, not a @BeforeClass: of several equal instances, TestNG runs the @BeforeClass
  // methods of the first one only, so the other instances would have no configuration result.
  @BeforeMethod
  public void setup() {
    if (skip) {
      throw new SkipException(name + " setup skipped");
    }
    throw new IllegalStateException(name + " setup failed");
  }

  @Test
  public void test() {}

  public String getName() {
    return name;
  }

  // The name is left out on purpose: every instance equals every other one.
  @Override
  public boolean equals(Object other) {
    return other instanceof EqualInstancesSample;
  }

  @Override
  public int hashCode() {
    return EqualInstancesSample.class.hashCode();
  }
}
