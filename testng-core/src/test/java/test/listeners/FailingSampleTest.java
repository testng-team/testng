package test.listeners;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(SimpleListener.class)
public class FailingSampleTest {
  @AfterMethod
  public void am() {
    SimpleListener.m_list.add(6);
  }

  @Test
  public void a1() {
    SimpleListener.m_list.add(4);
    throw new RuntimeException();
  }

}
