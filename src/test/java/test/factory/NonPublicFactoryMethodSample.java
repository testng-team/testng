package test.factory;

import org.testng.annotations.Factory;

public class NonPublicFactoryMethodSample {

    @Factory
    private Object[] createInstances() {
        return new Object[] {
                new BaseFactory(42),
                new BaseFactory(43)
        };
    }
}
