package test.tmp.p1;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class ContainerTest {

    @BeforeSuite
    public void startup() {
        assertNotNull(null);
    }

    @AfterSuite
    public void shutdown() {
        assertNotNull(null);
    }
}
