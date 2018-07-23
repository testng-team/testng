package test.newinstancepermethod;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SerialMemberModificationTest {
    private int member = 42;


    @Test
    public void fistTest() {
        try {
            Assert.assertEquals(member, 42);
        } finally {
            member++;
        }
    }

    @Test
    public void secondTest() {
        try {
            Assert.assertEquals(member, 42);
        } finally {
            member++;
        }
    }
}
