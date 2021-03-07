package test.thread.parallelization.sample;

import org.testng.TestNGException;
import org.testng.annotations.Factory;
import org.testng.internal.InstanceCreator;

import java.util.ArrayList;
import java.util.List;

public class FactoryForTestClassFSixMethodsWithNoDepsSixInstancesSample {
    @Factory
    public Object[] init() {
        List<Object> instances = new ArrayList<>();

        try {
            instances.add(InstanceCreator.newInstance(TestClassFSixMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassFSixMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassFSixMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassFSixMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassFSixMethodsWithNoDepsSample.class));
            instances.add(InstanceCreator.newInstance(TestClassFSixMethodsWithNoDepsSample.class));
        } catch (TestNGException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassFSixMethodsWithNoDepsSample", e
            );
        }

        return instances.toArray();
    }
}
