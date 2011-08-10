/*
 * @(#) TestClass.java
 * Created: Aug 10, 2011
 * By: Wolfgang & Monika Baltes
 * Copyright 2011 Wolfgang Baltes
 * WOLFGANG & MONIKA BALTES PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 */
package test.preserveorder;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author Wolfgang Baltes
 *
 */
public class TestClass {

    private final int val;

    public TestClass(final int val) {
        this.val = val;
    }

    @Test
    public void
    checkVal() {
        Assert.assertTrue(this.val != 0);
    }

}
