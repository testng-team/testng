package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class DepBugVerifyTest {

  @Test
  public void verify() {
    List<String> log = DepBugSampleTest.getLog();
    String[] expected = new String[] {"setup", "send", "get", "destroy"};
    for (int i = 0; i < log.size(); i++) {
      assertThat(log.get(i)).isEqualTo(expected[i]);
    }
  }
}
