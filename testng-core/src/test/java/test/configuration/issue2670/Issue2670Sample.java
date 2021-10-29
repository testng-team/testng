package test.configuration.issue2670;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "windows")
public class Issue2670Sample extends Issue2670BaseSample {

  @BeforeClass
  public void beforeClassChild() {}

  @Test
  public void aTestMethod() {}
}
