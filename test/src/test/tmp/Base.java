package test.tmp;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public abstract class Base {

    @BeforeGroups(value = "a", groups = "a")
    public void setUp() throws Exception {
        System.out.println("class is " + getClass().getName() + " Before group  ");
    }

    @AfterGroups(value = "a", groups = "a")
    public void tearDown(){
        System.out.println("class is " + getClass().getName() + " After group  ");
    }

 }


