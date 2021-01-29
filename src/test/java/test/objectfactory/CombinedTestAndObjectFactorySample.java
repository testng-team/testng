package test.objectfactory;

import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.testng.internal.InstanceCreator;

import java.lang.reflect.Constructor;

public class CombinedTestAndObjectFactorySample implements IObjectFactory {

  private boolean configured = false;

  @ObjectFactory
  public IObjectFactory create() {
    return new CombinedTestAndObjectFactorySample();
  }

  @Test
  public void isConfigured() {
    Assert.assertTrue(configured, "Should have been configured by object factory");
  }

  @Override
  public Object newInstance(Constructor<?> constructor, Object... params)  {
    try {
      Object o = InstanceCreator.newInstance(constructor, params);
      if (o instanceof CombinedTestAndObjectFactorySample) {
        CombinedTestAndObjectFactorySample s = (CombinedTestAndObjectFactorySample) o;
        s.configured = true;
      }
      return o;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
