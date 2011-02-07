package test.junit;

import org.testng.Assert;

import java.io.Serializable;

import junit.framework.TestCase;

public class SetNameTest extends TestCase implements Serializable {
  public static int m_ctorCount = 0;

  public SetNameTest() {
    ppp("CTOR");
    m_ctorCount++;
  }

  @Override
  public void setName(String name) {
    ppp("SETNAME " + name);
    super.setName(name);
  }

  public void testFoo() {
    Assert.assertEquals("testFoo", getName());
    ppp("FOO");
  }

  public void testBar() {
    Assert.assertEquals("testBar", getName());
    ppp("BAR");
  }

  private void ppp(String string) {
    if (false) {
      System.out.println("[FooBarTest#" + hashCode() + "] " + string);
    }
  }
}