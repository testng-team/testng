package test.preserveorder;

import org.testng.annotations.Test;

public class ChuckTest4 {

    @Test(groups = {"functional"}, dependsOnMethods = {"c4TestTwo"})
    public void c4TestThree() {
//        System.out.println("chucktest4: test three");
    }

    @Test(groups = {"functional"})
    public static void c4TestOne() {
//        System.out.println("chucktest4: test one");
    }

    @Test(groups = {"functional"}, dependsOnMethods = {"c4TestOne"})
    public static void c4TestTwo() {
//        System.out.println("chucktest4: test two");
    }

}
