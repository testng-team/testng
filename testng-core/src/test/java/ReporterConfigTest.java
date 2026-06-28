import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.testng.annotations.Test;
import org.testng.internal.ReporterConfig;

public class ReporterConfigTest {

  private static final String CLASS_NAME = "org.testng.reporters.XMLReporter";
  private static final String PROP_NAME_1 = "generateTestResultAttributes";
  private static final String PROP_NAME_2 = "generateGroupsAttribute";
  private static final String CONFIG_STR =
      CLASS_NAME + ":" + PROP_NAME_1 + "=true," + PROP_NAME_2 + "=true";

  @Test
  public void testDeserialize() {
    ReporterConfig config = ReporterConfig.deserialize(CONFIG_STR);
    assertThat(Objects.requireNonNull(config).getClassName()).isEqualTo(CLASS_NAME);
    String serial = config.serialize();
    assertThat(serial.contains(PROP_NAME_1 + "=true")).isTrue();
    assertThat(serial.contains(PROP_NAME_2 + "=true")).isTrue();
  }
}
