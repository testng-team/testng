package test.parameterrender;

import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ParameterOverride;
import org.testng.annotations.ParameterRender;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

/**
 * in this test, test method parameter will be updated by the ParameterRender
 * 
 * @author sayid
 */
public class Convert2GUIDTest {

    public static void main(String[] args) {
        TestNG testng = new TestNG();
        List<String> suites = Lists.newArrayList();
        suites.add("src/test/java/test/parameterrender/testng.xml");
        testng.setTestSuites(suites);
        testng.run();
    }

    @Test(dataProvider = "data")
    public void doStuff(@ParameterOverride(parameterRender = "convert2Guid") String s) {
        System.out.println(s);
        Assert.assertNotEquals(s, "[GUID]");
    }

    @ParameterRender(name = "convert2Guid")
    public String convert2Guid(String s) {
        if (s.equals("[GUID]")) {
            return UUID.randomUUID().toString();
        } else {
            return s;
        }
    }

    @DataProvider(name = "data")
    public Object[][] createData() {
        return new Object[][] { new Object[] { "abcd" }, new Object[] { "[GUID]" }, };
    }

}
