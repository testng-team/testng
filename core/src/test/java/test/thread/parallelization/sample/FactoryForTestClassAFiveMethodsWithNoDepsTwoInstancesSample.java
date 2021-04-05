package test.thread.parallelization.sample;

import org.testng.TestNGException;
import org.testng.annotations.Factory;
import org.testng.internal.objects.InstanceCreator;

import java.util.ArrayList;
import java.util.List;

public class FactoryForTestClassAFiveMethodsWithNoDepsTwoInstancesSample {

    @Factory
    public Object[] init() {
        List<Object> instances = new ArrayList<>();

        try {
            instances.add(InstanceCreator.newInstance(TestClassAFiveMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassAFiveMethodsWithNoDepsSample.class));
        } catch (TestNGException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassAFiveMethodsWithNoDepsSample", e
            );
        }

        return instances.toArray();
    }
}
