package test.attributes.issue2346;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SingleTest extends BaseTest {
  @BeforeMethod
  public void start() {
    fail();
  }

  @Test
  public void test() {}
}
