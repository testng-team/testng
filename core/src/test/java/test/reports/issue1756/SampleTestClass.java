package test.reports.issue1756;

import org.testng.ITest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.UUID;

@Listeners(CustomTestNGReporter.class)
public class SampleTestClass implements ITest {
    private static String uuid = UUID.randomUUID().toString();

    public static String getUuid() {
        return uuid;
    }

    private String uri;

    public SampleTestClass() {
        this.uri = uuid;
    }

    @Test
    public void test1() {
        throw new RuntimeException("failed");
    }

    @Test(dependsOnMethods = "test1")
    public void test2() { }

    public String getTestName() {
        return uri;
    }
}