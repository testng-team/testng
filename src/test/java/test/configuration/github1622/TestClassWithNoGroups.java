package test.configuration.github1622;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class TestClassWithNoGroups {

    @BeforeSuite
    public void failedBeforeSuite() {
        throw new RuntimeException();
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest() {
        throw new RuntimeException();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        throw new RuntimeException();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        throw new RuntimeException();
    }

    @Test
    public void testMethod() {
        System.out.println("I'm testMethod");
    }
}
