package test.lasttimeonly;

import org.testng.annotations.Test;
import test.BaseTest;

import static org.testng.Assert.assertEquals;

public class LastTimeOnlyTest extends BaseTest {

    @Test
    public void testLastTimeOnly() throws Exception {
        addClass(LastTimeOnly.class);
        run();
        assertEquals(getFailedTests().size(), 0);
        assertEquals(getPassedTests().size(), 3);
    }
}
