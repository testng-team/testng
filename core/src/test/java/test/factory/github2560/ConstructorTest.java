package test.factory.github2560;

import org.testng.annotations.*;

public class ConstructorTest {

    private final int hashCode;

    @Factory(dataProvider = "constructorArguments")
    public ConstructorTest(int hashCode) {
        this.hashCode = hashCode;
    }

    @DataProvider
    public static Object[][] constructorArguments() {
        return new Object[][]{{0}, {1}, {2}};
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
