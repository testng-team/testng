package test.factory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FactoryOrderSampleTest {

  int value;

  public FactoryOrderSampleTest(int j) {
    value = j;
    log("classC constructor " + value);
  }

  private void log(String string) {
//    System.out.println("[FactoryOrderSampleTest] " + string);
  }

  @BeforeClass(groups = { "s1ds" })
  public void setup() {
    log("classC.BeforeClass " + value);
  }

  @Test(groups = { "s1ds" })
  public void methodC1() throws Exception {
//    Thread.sleep(1000);
    log("classC.methodC1 " + value);
  }

  @AfterClass(groups = { "s1ds" })
  public void cleanup() {
    log("classC.AfterClass " + value);
  }

  @Override
  public String toString() {
    return "[FactoryOrderSampleTest " + value + "]";
  }

  public int getValue() {
    return value;
  }
}