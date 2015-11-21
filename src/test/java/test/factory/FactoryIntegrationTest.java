package test.factory;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class FactoryIntegrationTest extends SimpleBaseTest {

    @Test(description = "https://github.com/cbeust/testng/issues/876")
    public void testExceptionWithNonStaticFactoryMethod() {
        TestNG tng = create(GitHub876Sample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener(tla);

        try {
            tng.run();
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("A factory method MUST be static. But 'createInstances' from 'test.factory.GitHub876Sample' is not.");
        }
    }
}
