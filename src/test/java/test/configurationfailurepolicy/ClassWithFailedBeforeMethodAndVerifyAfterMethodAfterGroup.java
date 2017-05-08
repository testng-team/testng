package test.configurationfailurepolicy;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeMethodAndVerifyAfterMethodAfterGroup {

    static boolean tearDown = false;
    static boolean afterGroup = false;

    @BeforeMethod
    public void setupShouldFail() {
        throw new RuntimeException("Failing in setUp");
    }

    @Test(groups = "group1")
    public void test1() {

    }

    @AfterMethod
    public void tearDownShouldCall() {
        tearDown = true;
    }

    @AfterGroups(groups = "group1")
    public void groupShouldCall(){
        afterGroup = true;
    }

    static boolean sucess() {
        return tearDown && afterGroup;
    }
}
