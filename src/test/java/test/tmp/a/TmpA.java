package test.tmp.a;

import org.testng.annotations.Test;

public class TmpA {
  @Test(groups = "group-a")
  public void tmpa1() {
    System.out.println("tmpa1");
  }

  @Test
  public void tmpa2() {
    System.out.println("tmpa2");
  }
}