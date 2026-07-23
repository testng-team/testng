package test;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClassConfigurations {

  static int beforeCount = 0;
  static int afterCount = 0;

  @BeforeClass
  public void beforeTestClass() {
    ++beforeCount;
    //    System.out.println("@@@@@@ beforeTestClass has been called " + beforeCount + " time(s)");
  }

  @AfterTest
  public void afterTest() {
    beforeCount = 0;
    afterCount = 0;
  }

  @AfterTest
  public void afterTestClass() {
    ++afterCount;
    //    System.out.println("@@@@@@@ afterTestClass has been called " + afterCount + " time(s)");
  }

  @Test
  public void testOne() {
    assertThat(beforeCount).isEqualTo(1);
    assertThat(afterCount).isEqualTo(0);
  }

  @Test
  public void testTwo() {
    assertThat(beforeCount).isEqualTo(1);
    assertThat(afterCount).isEqualTo(0);
  }

  @Test
  public void testThree() {
    assertThat(beforeCount).isEqualTo(1);
    assertThat(afterCount).isEqualTo(0);
  }
}
