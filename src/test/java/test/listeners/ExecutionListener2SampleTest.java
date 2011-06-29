package test.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ExecutionListenerTest.ExecutionListener.class)
public class ExecutionListener2SampleTest {
  @Test
  public void f() {}

}
