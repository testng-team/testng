package test.factory.github2560;

import org.testng.annotations.Factory;

public class FactoryTestSample {

    @Factory
    public static Object[] factory() {
        return new Object[]{
                new TestClassSample(0),
                new TestClassSample(1),
                new TestClassSample(2)
        };
    }
}
