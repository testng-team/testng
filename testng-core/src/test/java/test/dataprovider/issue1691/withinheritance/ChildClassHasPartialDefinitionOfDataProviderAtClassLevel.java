package test.dataprovider.issue1691.withinheritance;

import org.testng.annotations.Test;
import test.dataprovider.issue1691.SampleDataProvider;

@Test(dataProviderClass = SampleDataProvider.class)
public class ChildClassHasPartialDefinitionOfDataProviderAtClassLevel
    extends BaseClassWithPartialDefinitionOfDataProviderInTestMethod {}
