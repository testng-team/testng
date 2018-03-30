package test.configuration;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import static org.assertj.core.api.Assertions.assertThat;

public class BeforeMethodWithGroupFiltersTest extends SimpleBaseTest {

    @Test
    public void beforeMethodWithBeforeGroupsShouldOnlyRunBeforeGroupMethods() {
        TestNG suite = create(new Class[] { BeforeMethodWithGroupFiltersSampleTest.class } );
        suite.run();
        for (String[] expectedSequence : BeforeMethodWithGroupFiltersSampleTest.EXPECTED_INVOCATION_SEQUENCES)
            assertThat(BeforeMethodWithGroupFiltersSampleTest.invocations)
                .containsSequence(expectedSequence);
        assertThat(BeforeMethodWithGroupFiltersSampleTest.invocations)
            .hasSize(BeforeMethodWithGroupFiltersSampleTest.EXPECTED_TOTAL_INVOCATIONS);
    }

}
