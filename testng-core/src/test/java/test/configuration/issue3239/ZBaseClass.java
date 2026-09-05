package test.configuration.issue3239;

import org.testng.annotations.BeforeClass;

/** The Z prefix is intentional: class-name sort must not hide the inheritance gap. */
abstract class ZBaseClass {

  @BeforeClass(alwaysRun = true, groups = "SomeTestGroup")
  protected final void zSetup() {}

  @BeforeClass(alwaysRun = true, dependsOnGroups = "SomeTestGroup")
  protected final void ySetup() {}
}
