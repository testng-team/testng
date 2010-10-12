package test.objectfactory;

import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;

@SuppressWarnings("serial")
public class CombinedTestAndObjectFactorySample implements IObjectFactory{
  private boolean configured = false;

  @ObjectFactory public IObjectFactory create() {
    return new CombinedTestAndObjectFactorySample();
  }

  @Test public void isConfigured() {
    Assert.assertTrue(configured, "Should have been configured by object factory");
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object newInstance(Constructor constructor, Object... params)  {
    Object o;
    try {
      o = constructor.newInstance(params);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (CombinedTestAndObjectFactorySample.class.equals(o.getClass())) {
      CombinedTestAndObjectFactorySample s = (CombinedTestAndObjectFactorySample) o;
      s.configured = true;
    }
    return o;
  }
}
