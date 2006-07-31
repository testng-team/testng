package test.dependent;

public class TestngBaseTest
{
    /**
     * @testng.configuration
     *              beforeTestClass = "true"
     */
    public void setUp() throws Exception {
//        System.out.println("++ TestngBaseTest.setUp");
    }

    /**
     * @testng.configuration
     *              afterTestClass = "true"
     */
    public void tearDown() throws Exception {
//        System.out.println("++ TestngBaseTest.tearDown");
    }

    /**
     * @testng.test
     */
    public void test0()
    {
//        System.out.println("+. TestngBaseTest.test0");
    }
}

