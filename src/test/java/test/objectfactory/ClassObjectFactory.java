package test.objectfactory;

import org.testng.IObjectFactory2;
import org.testng.internal.InstanceCreator;

import java.lang.reflect.Constructor;

public class ClassObjectFactory implements IObjectFactory2 {

  @Override
  public Object newInstance(Class<?> cls) {
    try {
      Constructor<?> ctor = cls.getConstructors()[0];
      return InstanceCreator.newInstance(ctor, 42);
    } catch(Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
