package test.objectfactory.github1131;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.testng.ITestObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.objects.ObjectFactoryImpl;

public class MyObjectFactory extends ObjectFactoryImpl {

  public static final List<Object[]> allParams = new ArrayList<>();

  @Override
  public <T> T newInstance(Constructor<T> constructor, Object... params) {
    allParams.add(params);
    return super.newInstance(constructor, params);
  }

  @ObjectFactory
  public ITestObjectFactory newInstance() {
    return new MyObjectFactory();
  }
}
