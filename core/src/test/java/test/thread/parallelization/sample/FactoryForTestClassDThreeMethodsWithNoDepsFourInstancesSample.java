package test.thread.parallelization.sample;

import org.testng.TestNGException;
import org.testng.annotations.Factory;
import org.testng.internal.objects.InstanceCreator;

import java.util.ArrayList;
import java.util.List;

public class FactoryForTestClassDThreeMethodsWithNoDepsFourInstancesSample {
    @Factory
    public Object[] init() {
        List<Object> instances = new ArrayList<>();

        try {
            instances.add(InstanceCreator.newInstance(TestClassDThreeMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassDThreeMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassDThreeMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassDThreeMethodsWithNoDepsSample.class));
        } catch (TestNGException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassDThreeMethodsWithNoDepsSample", e
            );
        }

        return instances.toArray();
    }
}
