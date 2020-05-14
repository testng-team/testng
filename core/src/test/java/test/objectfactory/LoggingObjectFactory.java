package test.objectfactory;

import org.testng.internal.ObjectFactoryImpl;

import java.lang.reflect.Constructor;

public class LoggingObjectFactory extends ObjectFactoryImpl {

  public static int invoked;

  @Override
  public Object newInstance(Constructor constructor, Object... params) {
    invoked++;
    return super.newInstance(constructor, params);
  }
}
