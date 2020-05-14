package test.mixed;

import org.testng.annotations.Test;

@Test(groups = {"unit"})
public class TestNGGroups {
    @Test
    public void tngTest1() {

    }

    @Test(groups = {"ignore"})
    public void tngShouldBeIgnored() {

    }

}
