package test.objectfactory;

import org.testng.IObjectFactory2;

import java.lang.reflect.Constructor;

public class ClassObjectFactory implements IObjectFactory2 {

  @Override
  public Object newInstance(Class<?> cls) {
    try {
      Constructor ctor = cls.getConstructors()[0];
      return ctor.newInstance(new Object[] { 42 });
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

}
