package test.tmp;

import org.testng.annotations.Test;

public class B extends Base{

    @Test(groups = "a")
    public void testB() throws Exception {
        System.out.println("class is " + getClass().getName() + " test  method is testB");
    }
 }