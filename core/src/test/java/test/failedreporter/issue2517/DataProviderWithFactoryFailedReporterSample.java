package test.failedreporter.issue2517;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class DataProviderWithFactoryFailedReporterSample {
    private Integer data;

    public DataProviderWithFactoryFailedReporterSample() {
    }

    @Factory(dataProvider = "dp")
    public DataProviderWithFactoryFailedReporterSample(Integer data) {
        this.data = data;
    }

    @DataProvider
    public Object[][] dp() {
        return new Object[][]{
                new Object[]{0}, new Object[]{1}, new Object[]{2},
        };
    }

    @Test
    public void f1() {
        if (data == 1) {
            throw new RuntimeException();
        }
    }
}
