package test.test111;


import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public abstract class AbstractTest {

	public static int R=0;
	
    @Test
    public void testAbstract() {
    }

    @AfterClass
    public void afterClass() {
        R++;
    }
}
