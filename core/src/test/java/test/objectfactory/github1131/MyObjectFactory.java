package test.objectfactory.github1131;

import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.ObjectFactoryImpl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
