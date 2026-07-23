package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class VerifyFactoryTest {

  @Test(dependsOnGroups = {"first"})
  public void mainCheck() {
    List<Integer> numbers = FactoryTest2.getNumbers();
    assertThat(numbers.contains(42)).withFailMessage("Didn't find 42").isTrue();
    assertThat(numbers.contains(43)).withFailMessage("Didn't find 43").isTrue();
    assertThat(numbers.size()).isEqualTo(2);
  }
}
