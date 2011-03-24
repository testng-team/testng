package org.testng.internal.junit;

import org.testng.AssertJUnit;

public class InexactComparisonCriteria extends ComparisonCriteria {
    public double fDelta;

    public InexactComparisonCriteria(double delta) {
        fDelta= delta;
    }

    @Override
    protected void assertElementsEqual(Object expected, Object actual) {
        if (expected instanceof Double)
            AssertJUnit.assertEquals((Double)expected, (Double)actual, fDelta);
        else
            AssertJUnit.assertEquals((Float)expected, (Float)actual, fDelta);
    }
}