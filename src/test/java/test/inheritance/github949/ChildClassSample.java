package test.inheritance.github949;

import org.testng.annotations.Test;

public class ChildClassSample extends ParentClassSample {

  @Override
  @Test
  public void independent() {
    logMessage();
  }
}
