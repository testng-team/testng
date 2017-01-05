package test.methodselectors;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.ArrayList;

public class BeanShellNegativeTest extends SimpleBaseTest {

    @BeforeMethod
    public void setup() {
        System.setProperty("skip.caller.clsLoader", Boolean.TRUE.toString());
    }

    @AfterMethod
    public void cleanup() {
        System.setProperty("skip.caller.clsLoader", Boolean.FALSE.toString());
    }

    @Test (expectedExceptions = TestNGException.class,
        expectedExceptionsMessageRegExp = ".*Please add a compile dependency.*")
    public void testNegativeScenario() {
        XmlSuite suite = createXmlSuite("suite");
        XmlTest test = createXmlTest(suite, "test", "test.methodselectors.SampleTest");
        final XmlMethodSelector selector = new XmlMethodSelector();
        selector.setLanguage("BeanShell");
        selector.setExpression("groups.containsKey(\"test1\")");
        test.setMethodSelectors(new ArrayList<XmlMethodSelector>() {{
            add(selector);
        }});
        TestNG tng = create(suite);
        Thread.currentThread().setContextClassLoader(new FakeClassLoader(true));
        tng.run();
    }

    public static class FakeClassLoader extends ClassLoader {
        private boolean skipBshLoading;

        FakeClassLoader(boolean skipBshLoading) {
            this.skipBshLoading = skipBshLoading;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (skipBshLoading && name.equals("bsh.Interpreter")) {
                throw new ClassNotFoundException("Simulating a missing jar");
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }

}
