package test.junit4;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author lukas
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JUnit4Sample1.class,
    JUnit4Sample2.class
})
public class JUnit4SampleSuite {

    public static final String[] EXPECTED = {"t1", "t2", "t4"};
    public static final String[] SKIPPED = {"t3", "ta"};
    public static final String[] FAILED = {"tf"};
}
