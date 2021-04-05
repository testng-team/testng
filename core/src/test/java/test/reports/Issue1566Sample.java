package test.reports;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Issue1566Sample {
    @DataProvider
    public Object[][] dataProvider() {
        return new Object[][]{{
                "test \u000C"
        }};
    }

    @Test(dataProvider = "dataProvider")
    public void test(String argument) {
    }
}
