package test.testng93;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class SingleTestTest {
  @BeforeMethod(groups={"group1"})
  public void shouldRunBefore() {
      System.out.println("Runs before");
  }

  @Test(groups={"group1"})
  public void theFirstActualTest() {
      System.out.println("The first actual test");
  }

  @Test
  public void theSecondActualTest() {
      System.out.println("The second actual test");
  }
}
