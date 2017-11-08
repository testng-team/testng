package test.parameters;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ParameterInjectAndOptionTest extends SimpleBaseTest {

    @Test
    public void test() {
        TestNG tng = create(ParameterInjectAndOptionSample.class);
        tng.run();
    }

}
