package test.github765;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;


public class ExcludeAbstractMethodsFromTemplateCallsTest extends SimpleBaseTest {

    @Test
    public void testMethod() {
        TestNG testng = create(DuplicateCallsSample.class);
        testng.run();
        Assert.assertFalse(DuplicateCallsSample.messages.isEmpty(), "Method invocation log should not be empty.");
        Assert.assertEquals(DuplicateCallsSample.messages.size(), 1);
        Assert.assertEquals(DuplicateCallsSample.messages.get(0), "Iteration [0], Test Parameter [4]");
    }
}
