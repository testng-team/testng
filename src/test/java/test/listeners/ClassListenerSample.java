package test.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyClassListener.class)
public class ClassListenerSample {

  @Test
  public void test() {}
}
