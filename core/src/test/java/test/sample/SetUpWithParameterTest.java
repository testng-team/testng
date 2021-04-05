package test.sample;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class fails in setUp and should result in 1 failure, 1 skip
 *
 * @author cbeust
 */
public class SetUpWithParameterTest {

  @BeforeClass
  public void setUp(String bogusParameter) {
  }

  @Test
  public void test() {

  }
}
