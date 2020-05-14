package test.retryAnalyzer.github1519;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

public class TestClassSample {
    static boolean retry = false;
    public static List<String> messages = Lists.newArrayList();

    @Test(retryAnalyzer = MyAnalyzer.class)
    public void testMethod() {
        Assert.assertTrue(retry);
    }
}