package test.dataprovider.issue3263;

import org.testng.annotations.Test;

@Test(dataProviderClass = DataProviderBeta.class)
public class SecondSubclassTestSample extends AbstractBaseTestSample {}
