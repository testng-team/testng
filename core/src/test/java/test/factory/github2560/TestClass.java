package test.factory.github2560;

import org.testng.annotations.*;

public class TestClass {

    private final int hashCode;

    public TestClass(int hashCode) {
        this.hashCode = hashCode;
    }

    @BeforeClass
    public void beforeClass() {
    }

    @BeforeMethod
    public void beforeMethod() {
    }

    @Test
    public void test() {
    }

    @AfterMethod
    public void afterMethod() {
    }

    @AfterClass
    public void afterClass() {
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
