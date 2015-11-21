package test.factory;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class FactoryIntegrationTest extends SimpleBaseTest {

    @Test(description = "https://github.com/cbeust/testng/issues/876")
    public void testExceptionWithNonStaticFactoryMethod() {
        TestNG tng = create(GitHub876Sample.class);
        try {
            tng.run();
            failBecauseExceptionWasNotThrown(TestNGException.class);
        } catch (TestNGException e) {
            assertThat(e).hasMessage("\nCan't invoke public java.lang.Object[] test.factory.GitHub876Sample.createInstances(): either make it static or add a no-args constructor to your class");
        }
    }

    @Test
    public void testNonPublicFactoryMethodShouldWork() {
        TestNG tng = create(NonPublicFactoryMethodSample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener(tla);

        tng.run();

        Assert.assertEquals(tla.getPassedTests().size(), 2);
    }

    @Test
    public void testExceptionWithBadFactoryMethodReturnType() {
        TestNG tng = create(BadFactoryMethodReturnTypeSample.class);
        try {
            tng.run();
            failBecauseExceptionWasNotThrown(TestNGException.class);
        } catch (TestNGException e) {
            assertThat(e).hasMessage("\ntest.factory.BadFactoryMethodReturnTypeSample.createInstances MUST return [ java.lang.Object[] or org.testng.IInstanceInfo[] ] but returns java.lang.Object");
        }
    }
}
