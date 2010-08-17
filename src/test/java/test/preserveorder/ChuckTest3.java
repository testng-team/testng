package test.preserveorder;

import org.testng.annotations.Test;

public class ChuckTest3 {

    @Test(groups = {"functional"}, dependsOnMethods = {"c3TestTwo"})
    public void c3TestThree() {
//        System.out.println("chucktest3: test three");
    }

    @Test(groups = {"functional"})
    public static void c3TestOne() {
//        System.out.println("chucktest3: test one");
    }

    @Test(groups = {"functional"}, dependsOnMethods = {"c3TestOne"})
    public static void c3TestTwo() {
//        System.out.println("chucktest3: test two");
    }

}
