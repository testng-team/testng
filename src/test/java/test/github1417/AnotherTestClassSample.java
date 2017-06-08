package test.github1417;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class AnotherTestClassSample {
    private String browsername;

    @Parameters({"browsername"})
    @BeforeClass
    public void LaunchBrowser(String browsername) {
        this.browsername = browsername;
    }

    @Test
    public void testMethod() {
        Assert.assertEquals("chrome", browsername);
    }
}
