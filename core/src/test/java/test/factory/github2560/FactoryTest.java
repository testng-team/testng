package test.factory.github2560;

import org.testng.annotations.Factory;

public class FactoryTest {

    @Factory
    public static Object[] factory() {
        return new Object[]{
                new TestClass(0),
                new TestClass(1),
                new TestClass(2)
        };
    }
}
