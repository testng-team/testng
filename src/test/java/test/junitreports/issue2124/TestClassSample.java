package test.junitreports.issue2124;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestClassSample {

  public static final String MESSAGE = "teenage mutant ninja turtles";

  @Test
  public void testReporter() {
    Reporter.log(MESSAGE, true);
  }

}
