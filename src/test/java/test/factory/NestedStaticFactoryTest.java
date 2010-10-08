package test.factory;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class NestedStaticFactoryTest {
  private int m_capacity = 2;
  private float m_loadFactor = 0.4f;

  static public class NestedStaticFactory {
    @Factory
    public Object[] createInstances() {
      return new NestedStaticFactoryTest[] {
        new NestedStaticFactoryTest(1, 0.1f),
        new NestedStaticFactoryTest(10, 0.5f),
      };
    }
  };

  private static int m_instanceCount = 0;
  public NestedStaticFactoryTest() {
    m_instanceCount++;
  }

  public NestedStaticFactoryTest(int capacity, float loadFactor) {
    m_instanceCount++;
   this.m_capacity=capacity;
   this.m_loadFactor=loadFactor;
  }

  @Test
  public void verify() {
    // Should have three instances:  the default one created by TestNG
    // and two created by the factory
    assertEquals(m_instanceCount, 2);
    assertTrue((m_capacity == 1 && m_loadFactor == 0.1f) ||
        m_capacity == 10 && m_loadFactor == 0.5f);
  }

  private static void ppp(String s) {
    System.out.println("[NestedStaticFactoryTest] " + s);
  }
}
