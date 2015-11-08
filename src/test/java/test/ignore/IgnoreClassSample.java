package test.ignore;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class IgnoreClassSample {

    @Test
    public void test() {}

    @Test
    public void test2() {}
}
