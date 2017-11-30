package test.configuration.github1625;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestclassSampleWithoutUsingMocks {

    private List<String> list;

    @BeforeClass
    public void beforeClass() {
        list = new ArrayList<>();
    }

    @Test
    public void first() {
        Assert.assertNotNull(list);
    }

    @Test
    public void second() {
        Assert.assertNotNull(list);
    }
}
