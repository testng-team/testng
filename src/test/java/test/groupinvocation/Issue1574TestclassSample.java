package test.groupinvocation;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class Issue1574TestclassSample {
    @BeforeSuite
    public void setupSuite() {
    }

    @BeforeTest
    public void setUpTest() {
    }

    @BeforeMethod
    public void setUpMethod() {
    }

    @AfterMethod
    public void tearDownMethod() {
    }

    @AfterTest
    public void tearDownTest() {
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
    }

    @Test(groups = {"sometest"})
    public void someTest() {
    }

    @Test(groups = {"anothertest"})
    public void anothertest() {
    }
}
