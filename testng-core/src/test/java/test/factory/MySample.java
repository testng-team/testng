package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class MySample {

  private final int i;

  // in this test, our default constructor sets s to a value that will cause a failure
  // the valid test instances should come from the factory
  public MySample() {
    i = 0;
  }

  public MySample(int i) {
    this.i = i;
  }

  @Test(groups = "MySample")
  public void testMethod() {
    FactoryInSeparateClassTest.addToSum(i);
    //    assert i > 0 : "MySample was not constructed with correct params";
    assertThat(i).withFailMessage("My test was not created by the factory").isNotZero();
  }

  @Test(dependsOnGroups = "testMethodOnFactoryClass")
  public void verifyThatTestMethodOnFactoryClassWasRun() {
    assertThat(FactoryInSeparateClassTest.wasRun())
        .withFailMessage("Test method on factory class wasn't run")
        .isTrue();
  }
}
