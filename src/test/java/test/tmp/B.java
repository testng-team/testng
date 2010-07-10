package test.tmp;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Listeners(BListener.class)
public class B extends SimpleBaseTest {

  @Test
  public void btest1() {
    System.out.println("B.btest1");
  }
}

