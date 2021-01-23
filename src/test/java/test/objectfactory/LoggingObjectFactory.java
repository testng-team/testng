package test.objectfactory;

import java.lang.reflect.Constructor;
import org.testng.internal.ObjectFactoryImpl;

public class LoggingObjectFactory extends ObjectFactoryImpl {

  public static int invoked;

  @Override
  public Object newInstance(Constructor constructor, Object... params) {
    invoked++;
    return super.newInstance(constructor, params);
  }
}
