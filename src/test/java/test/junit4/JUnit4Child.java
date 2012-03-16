package test.junit4;

import org.junit.runners.Suite;

/**
 *
 * @author lukas
 */
@Suite.SuiteClasses({JUnit4Sample1.class})
public class JUnit4Child extends JUnit4SampleSuite {
    public static final String[] EXPECTED = {"t1"};
}
