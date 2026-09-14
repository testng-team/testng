package org.testng.factory.samples.issue1770;

public class SampleTestBase {
  private String flag;

  public SampleTestBase(String fl) {
    this.flag = fl;
  }

  public String getFlag() {
    return flag;
  }
}
