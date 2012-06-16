package test.testng234;

import org.testng.annotations.Test;

public abstract class ParentTest {

  @Test
  public void executePolymorphicMethod() {
  }

  protected abstract void polymorphicMethod();

}
