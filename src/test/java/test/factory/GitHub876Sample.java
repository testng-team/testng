package test.factory;

import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class GitHub876Sample {

    @Factory
    public Object[] createInstances() {
        return new Object[]{
                new GitHub876Sample(new DataTest("foo", true)),
                new GitHub876Sample(new DataTest("FOO", false))
        };
    }

    private final DataTest dataTest;

    public GitHub876Sample(DataTest dataTest) {
        this.dataTest = dataTest;
    }

    @Test
    public void test() {
        switch (dataTest.s) {
            case "FOO":
                Assert.assertFalse(dataTest.b);
                break;
            case "foo":
                Assert.assertTrue(dataTest.b);
                break;
            default:
                Assert.fail("Unknown value");
        }
    }

    public static class DataTest {

        private final String s;
        private final boolean b;

        public DataTest(String s, boolean b) {
            this.s = s;
            this.b = b;
        }
    }
}
