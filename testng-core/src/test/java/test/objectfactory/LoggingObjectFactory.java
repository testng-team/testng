package test.objectfactory;

import java.lang.reflect.Constructor;
import org.testng.internal.objects.ObjectFactoryImpl;

public class LoggingObjectFactory extends ObjectFactoryImpl {

  public static int invoked;

  @Override
  public <T> T newInstance(Constructor<T> constructor, Object... params) {
    invoked++;
    return super.newInstance(constructor, params);
  }
}
