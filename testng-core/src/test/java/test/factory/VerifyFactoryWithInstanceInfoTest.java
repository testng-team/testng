package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class VerifyFactoryWithInstanceInfoTest {

  @Test(dependsOnGroups = {"first"})
  public void mainCheck() {
    List<Integer> numbers = FactoryWithInstanceInfo2Sample.getNumbers();
    assertThat(numbers).withFailMessage("Didn't find 42").contains(42);
    assertThat(numbers).withFailMessage("Didn't find 43").contains(43);
    assertThat(numbers).hasSize(2);
  }
}
