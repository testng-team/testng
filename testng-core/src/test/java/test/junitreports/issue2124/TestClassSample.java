package test.junitreports.issue2124;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestClassSample {

  public static final String MESSAGE_1 = "Teenage Mutant Ninja Turtles";
  public static final String MESSAGE_2 = "Teenage Mutant Ninja Turtles: <i>Out of the Shadows</i>";

  @Test
  public void testReporter() {
    Reporter.log(MESSAGE_1, true);
    Reporter.log(MESSAGE_2, true);
  }
}
