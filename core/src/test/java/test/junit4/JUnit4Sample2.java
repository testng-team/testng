package test.junit4;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author lukas
 */
public class JUnit4Sample2 {

    public static final String[] EXPECTED = {"t2", "t4"};
    public static final String[] SKIPPED = {"t3", "ta"};
    public static final String[] FAILED = {"tf"};

    @Test
    public void t2() {
    }

    @Test
    @Ignore
    public void t3() {
    }

    @Test
    public void t4() {
    }

    @Test
    public void tf() {
        Assert.fail("a test");
    }

    @Test
    public void ta() {
        Assume.assumeTrue(false);
    }
}
