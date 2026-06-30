package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Checks to see if the parameters from parent suite are passed onto the child suite (referred by
 * <suite-file>)
 */
public class InheritFromSuiteChild2 {
  @Test
  @Parameters({"parameter1", "parameter2", "parameter3", "parameter4"})
  public void inheritedParameter(String p1, String p2, String p3, @Optional("abc") String p4) {
    assertThat(p1).isEqualTo("p1");
    assertThat(p2).isEqualTo("c2p2");
    assertThat(p3).isEqualTo("c2p3");
    assertThat(p4).isEqualTo("abc");
  }
}
