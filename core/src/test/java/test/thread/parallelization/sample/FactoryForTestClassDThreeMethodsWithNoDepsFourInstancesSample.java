package test.thread.parallelization.sample;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryForTestClassDThreeMethodsWithNoDepsFourInstancesSample {
    @Factory
    public Object[] init() {
        List<Object> instances = new ArrayList<>();

        try {
            instances.add(TestClassDThreeMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassDThreeMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassDThreeMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassDThreeMethodsWithNoDepsSample.class.newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassDThreeMethodsWithNoDepsSample because it is " +
                            "abstract or for some other reason", e
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassDThreeMethodsWithNoDepsSample " +
                            "FactoryForTestClassDThreeMethodsWithNoDepsFourInstancesSample does not have access to its " +
                            "class definition", e
            );
        }

        return instances.toArray();
    }
}
