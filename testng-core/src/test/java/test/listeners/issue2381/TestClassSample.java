package test.listeners.issue2381;

import org.testng.IExecutionListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestClassSample.MyListener.class)
public class TestClassSample {

  @BeforeClass
  public void beforeClass() {}

  @Test
  public void testMethod() {}

  public static class MyListener implements IExecutionListener {}
}
