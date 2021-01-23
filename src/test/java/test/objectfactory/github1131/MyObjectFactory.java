package test.objectfactory.github1131;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.ObjectFactoryImpl;

public class MyObjectFactory extends ObjectFactoryImpl {

  public static final List<Object[]> allParams = new ArrayList<>();

  @Override
  public Object newInstance(Constructor constructor, Object... params) {
    allParams.add(params);
    return super.newInstance(constructor, params);
  }

  @ObjectFactory
  public IObjectFactory newInstance() {
    return new MyObjectFactory();
  }
}
