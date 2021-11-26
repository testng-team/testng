package test.objectfactory.issue2676;

import java.lang.reflect.Constructor;
import org.testng.ITestObjectFactory;
import org.testng.internal.objects.InstanceCreator;

public class LoggingObjectFactorySample implements ITestObjectFactory {

  public static boolean wasInvoked = false;

  @Override
  public <T> T newInstance(Constructor<T> constructor, Object... parameters) {
    wasInvoked = true;
    return InstanceCreator.newInstance(constructor, parameters);
  }
}
