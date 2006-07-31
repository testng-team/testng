package test.tmp.p1;

import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Configuration;

import javax.naming.*;

public class ContainerTest {

    @Configuration(beforeSuite = true)
    public void startup() {
        assertNotNull(null);
    }

    @Configuration(afterSuite = true)
    public void shutdown() {
        assertNotNull(null);
    }
}
