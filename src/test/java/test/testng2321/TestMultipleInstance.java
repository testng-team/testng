package test.testng2321;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestMultipleInstance {
    private int part;

    private long threadId;
    @BeforeSuite
    public void setProperty() {
        System.setProperty("testng.thread.affinity", "true");
    }

    @Factory(dataProvider = "dp")
    public TestMultipleInstance(int part) {
        this.part = part;
    }

    @Test
    public void independent() {
        threadId = Thread.currentThread().getId();
        System.err.println(getClass().getSimpleName() + " part - " + part + " independent() running on " + threadId);
    }

    @Test(dependsOnMethods = "independent")
    public void dependent() {
        long currentThreadId = Thread.currentThread().getId();
        System.err.println(getClass().getSimpleName() + " part - " + part + " dependent() running on " + currentThreadId);
        Assert.assertEquals(currentThreadId, threadId, "Thread Ids didn't match");
    }


    @DataProvider(name = "dp")
    public static Object[][] getData() {
        return new Object[][]{
                {1},
                {2}
        };
    }

    @AfterSuite
    public void removeProperty() {
        System.clearProperty("testng.thread.affinity");
    }
}
