package test.test111;


import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractTest {

	public static int R=0;
	
    @BeforeClass
    public void beforeClass() {
//        System.err.println("beforeClass");
    }

    @BeforeMethod
    public void beforeMethod() {
//        System.err.println("beforeMethod");
    }

    @Test
    public void testAbstract() {
//        System.err.println("Abstract TEST");
    }

    @AfterMethod
    public void afterMethod() {
//        System.err.println("afterMethod");
    }

    @AfterClass
    public void afterClass() {
//        System.err.println("afterClass");
        R++;
    }
}
