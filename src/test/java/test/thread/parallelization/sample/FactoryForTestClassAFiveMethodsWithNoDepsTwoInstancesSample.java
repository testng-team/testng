package test.thread.parallelization.sample;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryForTestClassAFiveMethodsWithNoDepsTwoInstancesSample {

    @Factory
    public Object[] init() {
        List<Object> instances = new ArrayList<>();

        try {
            instances.add(TestClassAFiveMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassAFiveMethodsWithNoDepsSample.class.newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassAFiveMethodsWithNoDepsSample because it is " +
                            "abstract or for some other reason", e
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassAFiveMethodsWithNoDepsSample " +
                            "FactoryForTestClassAFiveMethodsWithNoDepsTwoInstancesSample does not have access to its " +
                            "class definition", e
            );
        }

        return instances.toArray();
    }
}
