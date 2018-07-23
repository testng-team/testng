package test.newinstancepermethod;

import org.testng.Assert;
import org.testng.annotations.NewInstancePerMethod;
import org.testng.annotations.Test;

@NewInstancePerMethod
public class CloneableTest implements Cloneable {
    private boolean member = false;

    public Object clone() {
        CloneableTest clone = new CloneableTest();
        clone.member = true;
        return clone;
    }

    @Test
    public void checkCloningWorked() {
        Assert.assertTrue(member, "A cloned test should have made the member be true.");
    }
}
