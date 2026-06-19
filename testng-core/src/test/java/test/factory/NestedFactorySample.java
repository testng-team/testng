package test.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class NestedFactorySample {

  private final int m_capacity;
  private final float m_loadFactor;

  public static class NestedFactory {
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
    assertThat(m_instanceCount).isEqualTo(3);
    assertThat(tuple(m_capacity, m_loadFactor)).isIn(tuple(1, 0.1f), tuple(10, 0.5f));
  }
}
