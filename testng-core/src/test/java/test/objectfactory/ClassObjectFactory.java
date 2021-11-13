package test.objectfactory;

import java.lang.reflect.Constructor;
import org.testng.ITestObjectFactory;
import org.testng.internal.objects.InstanceCreator;

public class ClassObjectFactory implements ITestObjectFactory {

  public <T> T newInstance(Constructor<T> constructor, Object... parameters) {
    T object = InstanceCreator.newInstance(constructor, parameters);
    if (object instanceof ISetValue) {
      ((ISetValue) object).setValue(42);
    }
    return object;
  }
}
