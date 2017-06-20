package test.dataprovider;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IndicesTest extends SimpleBaseTest {

    @Test
    public void test() {
        InvokedMethodNameListener listener = run(IndicesSample.class);

        assertThat(listener.getFailedMethodNames()).isEmpty();
        List<String> expected = Arrays.asList(
                "indicesShouldWork(3)",
                "indicesShouldWorkWithIterator(3)"
        );
        for (String method : listener.getSucceedMethodNames()) {
            assertThat(method).isIn(expected);
        }
    }

    @Test
    public void test2() {
        XmlSuite xmlSuite = createXmlSuite("Suite");
        XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
        XmlClass xmlClass = createXmlClass(xmlTest, IndicesSample.class);
        createXmlInclude(xmlClass, "indicesShouldWork", /* index*/ 0, /* list */ 0);
        createXmlInclude(xmlClass, "indicesShouldWorkWithIterator", /* index*/ 0, /* list */ 0);

        TestNG tng = create(xmlSuite);

        InvokedMethodNameListener listener = new InvokedMethodNameListener();
        tng.addListener((ITestNGListener) listener);

        tng.run();

        assertThat(listener.getFailedMethodNames()).isEmpty();
        List<String> expected = Arrays.asList(
                "indicesShouldWork(1)", "indicesShouldWork(3)",
                "indicesShouldWorkWithIterator(1)", "indicesShouldWorkWithIterator(3)"
        );
        for (String method : listener.getSucceedMethodNames()) {
            assertThat(method).isIn(expected);
        }
    }
}