package test.inheritance.github949;

import org.testng.annotations.Test;

public class ChildClassWithAlwasyRunEnabledSample extends ParentClassWithAlwasyRunEnabledSample {

  @Override
  @Test
  public void independent() {
    logMessage();
  }
}
