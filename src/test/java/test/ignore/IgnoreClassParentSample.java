package test.ignore;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class IgnoreClassParentSample {

    @Test
    public void parentTest() {}
}
