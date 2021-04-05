package test.objectfactory.github1827;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class GitHub1827Test extends SimpleBaseTest {

    @Test(expectedExceptions = TestNGException.class,
            expectedExceptionsMessageRegExp = ".*Check to make sure it can be instantiated",
            description = "GITHUB-1827")
    public void ensureExceptionThrownWhenNoSuitableConstructorFound() {

        TestNG testng = create(GitHub1827Sample.class);
        testng.setVerbose(3);
        testng.run();
    }
}
