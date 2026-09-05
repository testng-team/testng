package org.testng.groups.samples.issue182;

import org.testng.annotations.Test;

@Test(groups = "myGroup")
public class ChildTest extends ParentTest {

  public void childTestMethod() {}
}
