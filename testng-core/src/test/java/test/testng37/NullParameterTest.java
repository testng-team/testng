package test.testng37;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/** This class/interface */
public class NullParameterTest {
  @Test
  @Parameters({"notnull", "nullvalue"})
  public void nullParameter(String notNull, int mustBeNull) {
    assertThat(notNull).withFailMessage("not null parameter expected").isNotNull();
    assertThat(mustBeNull).withFailMessage("null parameter expected").isNull();
  }
}
