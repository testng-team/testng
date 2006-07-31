package test.factory;

import org.testng.Assert;
import org.testng.IInstanceInfo;


public class IInstanceFactory {
  private static boolean m_invoked= false;
  /**
   * @testng.factory
   */
  public Object[] createObjects() {
    return new IInstanceInfo[] {
      new MyInstanceInfo(TestInterface.class, new TestInterfaceImpl())
    };
  }
  
  /**
   * @testng.configuration afterTest="true"
   */
  public void afterVerification() {
    Assert.assertTrue(m_invoked, "implementation of TestInterface test method should have been invoked");
  }
  
  public static class MyInstanceInfo implements IInstanceInfo {
    private Class m_clazz;
    private Object m_instance;
    
    public MyInstanceInfo(Class clazz, Object instance) {
      m_clazz= clazz;
      m_instance= instance;
    }

    public Object getInstance() {
      return m_instance;
    }

    public Class getInstanceClass() {
      return m_clazz;
    }
  }
  
  public static interface TestInterface {
    /**
     * @testng.test
     */
    void testMethod();
  }
  
  public static class TestInterfaceImpl implements TestInterface {

    public void testMethod() {
      m_invoked= true;
    }
    
  }
}
