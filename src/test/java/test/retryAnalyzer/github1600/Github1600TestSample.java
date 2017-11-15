package test.retryAnalyzer.github1600;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Github1600TestSample {
    private static int a = 2;

    @Test
    public void test1() {
        Assert.assertEquals(a, 2);
        a++;
    }

}