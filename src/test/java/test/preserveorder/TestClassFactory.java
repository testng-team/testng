package test.preserveorder;

import org.testng.annotations.Factory;

public class TestClassFactory {

    @Factory
    public Object[] f() {
        final Object[] res = new Object[4];
        for (int i = 1; i < 5; i++) {
            res[i-1] = new TestClass(i);
        }
        return res;
    }
}
