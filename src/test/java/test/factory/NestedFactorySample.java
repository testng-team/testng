package test.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class NestedFactorySample {

  private final int m_capacity;
  private final float m_loadFactor;

  public class NestedFactory {
    @Factory
    public Object[] createInstances() {
      return new NestedFactorySample[] {
        new NestedFactorySample(1, 0.1f), new NestedFactorySample(10, 0.5f),
      };
    }
  }

  private static int m_instanceCount = 0;

  public NestedFactorySample() {
    this(2, 0.4f);
  }

  public NestedFactorySample(int capacity, float loadFactor) {
    m_instanceCount++;
    this.m_capacity = capacity;
    this.m_loadFactor = loadFactor;
  }

  @Test
  public void verify() {
    // Should have three instances:  the default one created by TestNG
    // and two created by the factory
    assertEquals(m_instanceCount, 3);
    assertTrue(
        (m_capacity == 1 && m_loadFactor == 0.1f) || m_capacity == 10 && m_loadFactor == 0.5f);
  }
}
