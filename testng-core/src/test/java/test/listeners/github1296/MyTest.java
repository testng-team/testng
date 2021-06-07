package test.listeners.github1296;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyConfigurationListener.class)
public class MyTest {

  @BeforeTest
  public void setUp() {}

  @Test
  public void test() {
    assertTrue(true);
  }

  @AfterTest
  public void tearDown() {}
}
