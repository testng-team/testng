package test.dependent;

public class TestngBaseTest
{
    /**
     * @testng.before-class
     */
    public void setUp() throws Exception {
//        System.out.println("++ TestngBaseTest.setUp");
    }

    /**
     * @testng.after-class
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

