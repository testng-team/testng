package test.tmp;

import org.testng.annotations.Test;

@Test(dependsOnGroups = { "G0" })
public class Test1 {

       public void test() {
               System.out.println("Test1.test");
       }

}