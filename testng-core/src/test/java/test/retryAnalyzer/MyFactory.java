package test.retryAnalyzer;

import org.testng.annotations.Factory;

public class MyFactory {
    @Factory
    public Object[] createTests() {
        int num = 10;
        Object[] result = new Object[num];
        for (int i = 0; i < num; i++) {
            FactoryTest obj = new FactoryTest("Test" + i);
            result[i] = obj;
        }
        return result;
    }
}
