package test.issue107;

import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Listeners(MySuiteListener.class)
public class TestTestngCounter {

  @Parameters({ "hello" })
  @Test
  public void shouldLogSimpleText(@Optional("Unknown") String hello) {
    System.out.println("Hello World!");
  }
}
