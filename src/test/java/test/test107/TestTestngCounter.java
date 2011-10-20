package test.test107;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class TestTestngCounter {

	@Parameters({ "hello" })
	@Test
	public void shouldLogSimpleText(@Optional("Unknown") String hello) {
		System.out.println("Hello World!"+hello);
	}
}