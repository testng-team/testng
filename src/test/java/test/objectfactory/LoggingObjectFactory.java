package test.objectfactory;

import org.testng.internal.ObjectFactoryImpl;

import java.lang.reflect.Constructor;

public class LoggingObjectFactory extends ObjectFactoryImpl {

  private static final long serialVersionUID = -395096650866727480L;
  public static int invoked;

  @Override
  public Object newInstance(Constructor constructor, Object... params) {
    invoked++;
    return super.newInstance(constructor, params);
  }
}
