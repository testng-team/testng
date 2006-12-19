package test.tmp;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class A extends Base{

    @Test(groups = "a")
    public void testA() throws Exception {
        System.out.println("class is " + getClass().getName() + " test method is testA");
    }
 }
 
