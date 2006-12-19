package test.regression.groupsordering;

import org.testng.annotations.Test;

import test.tmp.Base;

public class B extends Base{

    @Test(groups = "a")
    public void testB() throws Exception {
        System.out.println("class is " + getClass().getName() + " test  method is testB");
    }
 }