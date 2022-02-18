package test.configuration.issue2729;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({BeforeConfigSampleListener.class})
public class BeforeConfigTestSample {
  @BeforeClass
  public void beforeClass() {
    int i = 5 / 0;
  }

  @BeforeMethod
  public void beforeMethod() {}

  @Test
  public void sampleTest() {}
}
