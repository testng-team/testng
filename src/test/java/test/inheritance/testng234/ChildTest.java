package test.inheritance.testng234;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ChildTest extends ParentTest {

  @BeforeClass
  public void beforeClassMethod() {
    Assert.assertTrue(false, "This is so sad... I must skip all my tests ...");
  }

  @Override
  @Test
  public void polymorphicMethod() {
  }

}
