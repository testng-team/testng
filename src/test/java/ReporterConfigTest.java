import java.util.Objects;
import org.testng.IReporter;
import org.testng.internal.ReporterConfig;
import org.testng.annotations.Test;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLReporterConfig;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReporterConfigTest {

    private static final String CLASS_NAME = "org.testng.reporters.XMLReporter";
    private static final String PROP_NAME_1 = "generateTestResultAttributes";
    private static final String PROP_NAME_2 = "generateGroupsAttribute";
    private static final String CONFIG_STR = CLASS_NAME + ":" + PROP_NAME_1 + "=true," + PROP_NAME_2 + "=true";

    @Test
    public void testDeserialize() {
        ReporterConfig config = ReporterConfig.deserialize(CONFIG_STR);
        assertEquals(Objects.requireNonNull(config).getClassName(), CLASS_NAME);
        String serial = config.serialize();
        assertTrue(serial.contains(PROP_NAME_1 + "=true"));
        assertTrue(serial.contains(PROP_NAME_2 + "=true"));
    }
}
