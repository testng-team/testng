package test.triangle;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * this test checks the CountCalls's static counter to see if we got the expected number of calls
 */
public class CheckCount {

  @Parameters({"expected-calls"})
  @Test
  public void testCheckCount(String expectedCalls) {
    int i = Integer.valueOf(expectedCalls);
    int numCalls = CountCalls.getNumCalls();
    assertThat(numCalls).isEqualTo(i);
  }
}
