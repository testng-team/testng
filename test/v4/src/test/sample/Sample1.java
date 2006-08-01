package test.sample;


import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

/**
 * This class
 *
 * @author Cedric Beust, Apr 26, 2004
 * 
 */
public class Sample1 extends BaseSample1 {
	@Configuration(afterTestClass = true) 
	public static void tearDownClass1() {
	}

	@Configuration(afterTestClass = true) 
	public void tearDownClass2() {
	}

	@Configuration(beforeTestMethod = true) 
	public void beforeTest() {
	}

	@Configuration(afterTestMethod = true) 
	public void afterTest() {
	}

	@Test(groups = {"even"}) 
	public void method2() {
	}
	
	@Test(groups = {"odd"}) 
	public void method3() {
	}

	@Test(groups = {"odd"}, enabled = false) 
	public void oddDisableMethod() {
	}

	@Test(groups = {"broken"}) 
	public void broken() {
	}
	
	@Test(groups = {"fail"}) 
	@ExpectedExceptions( {NumberFormatException.class,	ArithmeticException.class}) 
	public void throwExpectedException1ShouldPass() {
		throw new NumberFormatException();
	}

	@Test(groups = {"fail"}) 
	@ExpectedExceptions( {NumberFormatException.class,	ArithmeticException.class}) 
	public void throwExpectedException2ShouldPass() {
		throw new ArithmeticException();
	}

	@Test(groups = {"fail", "bug"}) 
	public void throwExceptionShouldFail() {
		throw new NumberFormatException();
	}

	@Test(groups = {"assert"}) 
	public void verifyLastNameShouldFail() {
		assert "Beust".equals("") : "Expected name Beust, found blah";
	}

	private static void ppp(String s) {
		System.out.println("[Test1] " + s);
	}

}
