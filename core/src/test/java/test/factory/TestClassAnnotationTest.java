package test.factory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Make sure that @Factory methods are not counted as @Test in the presence of a class-scoped @Test
 * annotation.
 */
@Test
public class TestClassAnnotationTest {

  private int count;

  @Factory
  public Object[] createFixture() {
    count++;
    return new Object[] {new Object[] {new Object()}};
  }

  public void testOne() {
    count++;
  }

  @AfterClass
  public void verify() {
    Assert.assertEquals(count, 2);
  }
}
