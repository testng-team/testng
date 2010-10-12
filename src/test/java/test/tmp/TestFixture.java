package test.tmp;

import org.testng.annotations.Configuration;

public class TestFixture {

  public static int globalCallSequence = 0;

  public static int printGlobalCallSequence(String methodName) {
    globalCallSequence++;
    System.err.println("*** " + methodName + ": globalCallSequence=" + globalCallSequence);
    return globalCallSequence;
  }

  public int fixtureBeforeTestCallSequence;

  @Configuration(beforeTest=true, groups="fixture")
  public void beforeTest() {
    fixtureBeforeTestCallSequence = printGlobalCallSequence("TestFixture.beforeTest");
  }

  public int getSomeRandomValue() {
    return 20;
  }

}
