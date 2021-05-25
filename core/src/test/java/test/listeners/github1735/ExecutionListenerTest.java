package test.listeners.github1735;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionListenerTest extends SimpleBaseTest {
    @Test
    public void ensureExecutionListenerIsInvokedOnlyOnce() {
        XmlSuite suite = createXmlSuite("suite");
        createXmlTest(suite, "test1", TestClassSample.class, TestClassTwoSample.class);
        createXmlTest(suite, "test2", TestClassSample.class, TestClassTwoSample.class);
        TestNG testng = create(suite);
        testng.run();
        assertThat(LocalExecutionListener.getFinish()).containsExactlyElementsOf(Collections.singletonList("finish"));
        assertThat(LocalExecutionListener.getStart()).containsExactlyElementsOf(Collections.singletonList("start"));
    }

}
