package test.retryAnalyzer.issue1538;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestClassSampleWithTestMethodDependencies {
    private int i = 0;

    @Test(retryAnalyzer = RetryForIssue1538.class)
    public void a() {
        Assert.assertEquals(i++, 1);
    }

    @Test(dependsOnMethods = "a", retryAnalyzer = RetryForIssue1538.class)
    public void b() {
        Assert.assertEquals(i++, 2);
    }
}
