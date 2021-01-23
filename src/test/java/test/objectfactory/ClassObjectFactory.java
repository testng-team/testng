package test.objectfactory;

import java.lang.reflect.Constructor;
import org.testng.IObjectFactory2;

public class ClassObjectFactory implements IObjectFactory2 {

  @Override
  public Object newInstance(Class<?> cls) {
    try {
      Constructor ctor = cls.getConstructors()[0];
      return ctor.newInstance(42);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
