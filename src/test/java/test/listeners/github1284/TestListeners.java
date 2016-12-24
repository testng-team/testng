package test.listeners.github1284;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import java.util.Collections;

public class TestListeners extends SimpleBaseTest {

    @Test
    public void verifyWithoutListener() {
        TestNG testNG = create();
        testNG.setTestSuites(Collections.singletonList(getPathToResource("test/listeners/github1284/github1284_nolistener.xml")));
        testNG.run();
        Assert.assertEquals(testNG.getStatus(), 0);
    }

    @Test(dependsOnMethods = "verifyWithoutListener")
    public void verifyWithListener() {
        TestNG testNG = create();
        testNG.setTestSuites(Collections.singletonList(getPathToResource("test/listeners/github1284/github1284_withlistener.xml")));
        testNG.run();
        Assert.assertEquals(testNG.getStatus(), 0);
    }

    @Test(dependsOnMethods = "verifyWithListener")
    public void verifyWithChildSuite() {
        TestNG testNG = create();
        testNG.setTestSuites(Collections.singletonList(getPathToResource("test/listeners/github1284/github1284.xml")));
        testNG.run();
        Assert.assertEquals(testNG.getStatus(), 0);
    }

}
