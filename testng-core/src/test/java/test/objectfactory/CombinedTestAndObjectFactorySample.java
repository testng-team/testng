package test.objectfactory;

import java.lang.reflect.Constructor;
import org.testng.Assert;
import org.testng.ITestObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.testng.internal.objects.InstanceCreator;

public class CombinedTestAndObjectFactorySample implements ITestObjectFactory {

  private boolean configured = false;

  @ObjectFactory
  public ITestObjectFactory create() {
    return new CombinedTestAndObjectFactorySample();
  }

  @Test
  public void isConfigured() {
    Assert.assertTrue(configured, "Should have been configured by object factory");
  }

  @Override
  public <T> T newInstance(Constructor<T> constructor, Object... params) {
    try {
      T o = InstanceCreator.newInstance(constructor, params);
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
