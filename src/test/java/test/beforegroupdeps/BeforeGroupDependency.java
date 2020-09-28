package test.beforegroupdeps;

import static org.testng.Assert.*;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class BeforeGroupDependency {
  private boolean z1run = false;
  private boolean z2run = false;
  private boolean setupBrun = false;
  private boolean a1run = false;
  private boolean a2run = false;

  @Test(groups="Z")
  public void z1() {
    System.out.println("z1");
    assertFalse(setupBrun);
    assertFalse(a1run);
    assertFalse(a2run);
    z1run = true;
  }

  @Test(groups="Z")
  public void z2() {
    System.out.println("z2");
    assertFalse(setupBrun);
    assertFalse(a1run);
    assertFalse(a2run);
    z2run = true;
  }

  @BeforeGroups(value="A", dependsOnGroups="Z")
  public void setupA() {
    System.out.println("setupB");
    assertTrue(z1run);
    assertTrue(z2run);
    assertFalse(a1run);
    assertFalse(a2run);
    setupBrun = true;
  }

  @Test(groups="A")
  public void a1() {
    System.out.println("a1");
    assertTrue(z1run);
    assertTrue(z2run);
    assertTrue(setupBrun);
    a1run = true;
  }

  @Test(groups="A")
  public void a2() {
    System.out.println("a2");
    assertTrue(z1run);
    assertTrue(z2run);
    assertTrue(setupBrun);
    a2run = true;
  }

}

