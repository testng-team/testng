package test.inheritance.testng234;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ChildTest extends ParentTest {

  @BeforeClass
  public void beforeClassMethod() {
    assertThat(false).withFailMessage("This is so sad... I must skip all my tests ...").isTrue();
  }

  @Override
  @Test
  public void polymorphicMethod() {}
}
