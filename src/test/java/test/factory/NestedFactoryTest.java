package test.factory;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class NestedFactoryTest {
  private int m_capacity = 2;
  private float m_loadFactor = 0.4f;

  public class NestedFactory {
    @Factory
    public Object[] createInstances() {
      return new NestedFactoryTest[] {
        new NestedFactoryTest(1, 0.1f),
        new NestedFactoryTest(10, 0.5f),
      };
    }
  };

  private static int m_instanceCount = 0;
  public NestedFactoryTest() {
    m_instanceCount++;
  }

  public NestedFactoryTest(int capacity, float loadFactor) {
    m_instanceCount++;
   this.m_capacity=capacity;
   this.m_loadFactor=loadFactor;
  }

  @Test
  public void verify() {
    // Should have three instances:  the default one created by TestNG
    // and two created by the factory
    assertEquals(m_instanceCount, 3);
    assertTrue((m_capacity == 1 && m_loadFactor == 0.1f) ||
        m_capacity == 10 && m_loadFactor == 0.5f);
  }

  private static void ppp(String s) {
    System.out.println("[NestedFactoryTest] " + s);
  }
}
