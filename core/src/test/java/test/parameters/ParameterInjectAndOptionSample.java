package test.parameters;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

public class ParameterInjectAndOptionSample {

    @BeforeSuite
    @Parameters({ "beforesuitedata" })
    public void beforeSuite(ITestContext context, @Optional("optionalbeforesuitedata") String beforesuitedata) {
        Assert.assertEquals(beforesuitedata, "optionalbeforesuitedata");
    }

    @Test
    @Parameters({ "testdata" })
    public void test(XmlTest xmlTest, @Optional("optionaltestdata") String testdata) {
        Assert.assertEquals(testdata, "optionaltestdata");
    }

}
