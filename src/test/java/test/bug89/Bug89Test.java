package test.bug89;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

public class Bug89Test extends SimpleBaseTest {

    @Test(description = "Fix for https://github.com/cbeust/testng/issues/89")
    public void methodsShouldBeSequential() {
        XmlSuite s = createXmlSuite("Bug89");
		Class [] tests = new Class[1];
		tests[0]=C.class;
        TestNG tng = create();
        tng.setParallel("classes");
        tng.setTestClasses(tests);   
        tng.run();
        Assert.assertTrue(B.isSameThread);
    }
}