package test.retryAnalyzer;

import static org.testng.Assert.fail;

import org.testng.ITest;
import org.testng.annotations.Test;

public class FactoryTest implements ITest {
    public static int m_count = 0;

    private String name;

    public FactoryTest(String name) {
        this.name = name;
    }

    @Override
    public String getTestName() {
        return name;
    }

    @Test(retryAnalyzer = MyRetry.class)
    public void someTest1() {
        System.out.println("Test Called : " + this.name);
        if (name.contains("5")) {
            m_count++;
            fail();
        }
    }
}
