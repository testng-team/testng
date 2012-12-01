package test.sample;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author lukas
 */
public abstract class JUnitSample4 extends TestCase {

    private int i = 0;

    public JUnitSample4(String name, int i) {
        super(name);
        this.i = i;
    }

    public void testXY() {
        Assert.assertEquals(1, 1);
    }

    public static TestSuite suite() {
        TestSuite ts = new TestSuite("Sample Suite");
        for (int i = 0; i < 3; i++) {
            ts.addTest(new T(i));
        }
        return ts;
    }

    private static class T extends JUnitSample4 {

        public T(int i) {
            super("testXY", i);
        }
    }
}
