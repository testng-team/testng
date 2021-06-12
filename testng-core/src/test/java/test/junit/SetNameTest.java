package test.junit;

import junit.framework.TestCase;
import org.testng.Assert;

public class SetNameTest extends TestCase {
  public static int m_ctorCount = 0;

  public SetNameTest() {
    m_ctorCount++;
  }

  @Override
  public void setName(String name) {
    super.setName(name);
  }

  public void testFoo() {
    Assert.assertEquals("testFoo", getName());
  }

  public void testBar() {
    Assert.assertEquals("testBar", getName());
  }
}
