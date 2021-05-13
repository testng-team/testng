package test.justin;

import org.testng.annotations.Test;

import java.text.ParseException;

/**
 * Created Jul 10, 2005
 *
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a>
 */
public class MonthTest extends BaseTestCase {
    public MonthTest() {
    }

    public MonthTest(String name) {
        super(name);
    }

    @Test(groups = {"bean-tests"})
    public void july2005() throws ParseException {
    }

    @Test
    public void weekendDay() throws ParseException {
    }
}