package test.dependent;

public class TestngTest extends TestngBaseTest
{

    /**
     * @testng.configuration
     *              beforeTestClass = "true"
     */
    public void setUp() throws Exception {
//        System.out.println("++ TestngTest.setUp");
    }

    /**
     * @testng.configuration
     *              afterTestClass = "true"
     */
    public void tearDown() throws Exception {
//        System.out.println("++ TestngTest.tearDown");
    }

    /**
     * @testng.test
     */
    public void test1()
    {
//        System.out.println("+. TestngTest.test1");
    }

    /**
     * @testng.test
     */
    public void test2()
    {
//        System.out.println("+. TestngTest.test2");
    }
}