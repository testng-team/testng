package org.testng.internal.junit;

import org.testng.AssertJUnit;

public class ExactComparisonCriteria extends ComparisonCriteria {
    @Override
    protected void assertElementsEqual(Object expected, Object actual) {
        AssertJUnit.assertEquals(expected, actual);
    }
}
