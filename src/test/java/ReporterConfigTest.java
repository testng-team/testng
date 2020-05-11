import org.testng.IReporter;
import org.testng.ReporterConfig;
import org.testng.annotations.Test;
import org.testng.reporters.XMLReporter;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReporterConfigTest {

    private static final String CLASS_NAME = "org.testng.reporters.XMLReporter";
    private static final String PROP_NAME_1 = "config.generateTestResultAttributes";
    private static final String PROP_NAME_2 = "config.generateGroupsAttribute";
    private static final String CONFIG_STR = CLASS_NAME + ":" + PROP_NAME_1 + "=true," + PROP_NAME_2 + "=true";

    @Test
    public void testDeserialize() {
        ReporterConfig config = ReporterConfig.deserialize(CONFIG_STR);
        assertEquals(config.getClassName(), CLASS_NAME);
        List<ReporterConfig.Property> properties = config.getProperties();
        assertEquals(properties.size(), 2);
        String serial = config.serialize();
        assertTrue(serial.contains(PROP_NAME_1 + "=true"));
        assertTrue(serial.contains(PROP_NAME_2 + "=true"));
    }

    @Test
    public void testInstantiate() {
        ReporterConfig config = ReporterConfig.deserialize(CONFIG_STR);
        IReporter reporter = config.newReporterInstance();
        assertTrue(reporter instanceof XMLReporter);
    }
}
