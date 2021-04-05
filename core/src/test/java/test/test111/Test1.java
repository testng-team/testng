package test.test111;

import org.testng.annotations.Test;

import junit.framework.Assert;

public class Test1 extends AbstractTest {
    @Test
    public void test() {
    	Assert.assertEquals(0, AbstractTest.R);
    }
}