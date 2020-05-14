package test.thread.parallelization.sample;

import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryForTestClassFSixMethodsWithNoDepsSixInstancesSample {
    @Factory
    public Object[] init() {
        List<Object> instances = new ArrayList<>();

        try {
            instances.add(TestClassFSixMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassFSixMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassFSixMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassFSixMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassFSixMethodsWithNoDepsSample.class.newInstance());
            instances.add(TestClassFSixMethodsWithNoDepsSample.class.newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassFSixMethodsWithNoDepsSample because it is " +
                            "abstract or for some other reason", e
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Could not instantiate an instance of TestClassFSixMethodsWithNoDepsSample " +
                            "FactoryTestClassFSixMethodsWithNoDepsThreeInstancesSample does not have access to its " +
                            "class definition", e
            );
        }

        return instances.toArray();
    }
}
