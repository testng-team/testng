package test.dependent;

public class Test2
{

    /**
     * @testng.test
     *              beforeTestClass = "true"
     *              groups="init.wt"
     */
    public void init1()
    {
        System.out.println("* init1");
//        assert false : "error";
    }

    /**
     * @testng.test
     *              beforeTestClass = "true"
     *              groups="init.wt"
     *              dependsOnMethods="init1"
     */
    public void init2()
    {
        System.out.println("* init2");
    }

    /**
     * @testng.test
     *              afterTestClass = "true"
     *              groups="end.wt"
     *              dependsOnMethods="init1"
     */
    public void end1()
    {
        System.out.println("* end1");
    }

    /**
     * @testng.test groups="test"
     *              dependsOnGroups="init.*"
     */
    public void test1()
    {
        System.out.println("* test1");
    }

    /**
     * @testng.test groups="test"
     *              dependsOnGroups="init.*"
     */
    public void test2()
    {
        System.out.println("* test2");
    }
}