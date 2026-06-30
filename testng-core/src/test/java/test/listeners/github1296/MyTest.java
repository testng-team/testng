package test.listeners.github1296;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(true).isTrue();
  }

  @AfterTest
  public void tearDown() {}
}
