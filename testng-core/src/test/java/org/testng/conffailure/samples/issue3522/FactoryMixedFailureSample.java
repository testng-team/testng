package org.testng.conffailure.samples.issue3522;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryMixedFailureSample {

  private final String id;

  private FactoryMixedFailureSample(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  @Factory
  public static Object[] instances() {
    return new Object[] {
      new FactoryMixedFailureSample("method"), new FactoryMixedFailureSample("class")
    };
  }

  @BeforeClass
  public void beforeClass() {
    if ("class".equals(id)) {
      throw new RuntimeException("class instance beforeClass fails");
    }
  }

  @BeforeMethod(ignoreFailure = true)
  public void beforeMethod() {
    if ("method".equals(id)) {
      throw new RuntimeException("method instance beforeMethod fails");
    }
  }

  @Test
  public void test() {}
}
