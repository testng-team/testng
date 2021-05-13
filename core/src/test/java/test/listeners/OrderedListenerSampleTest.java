package test.listeners;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Listeners(SimpleListener.class)
public class OrderedListenerSampleTest extends SimpleBaseTest {

  @BeforeMethod
  public void bm() {
    SimpleListener.m_list.add(1);
  }

  @Test
  public void f() {
    SimpleListener.m_list.add(2);
  }

  @AfterMethod
  public void am() {
    SimpleListener.m_list.add(4);
  }
}

