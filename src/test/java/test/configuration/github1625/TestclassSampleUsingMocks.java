package test.configuration.github1625;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class TestclassSampleUsingMocks {
    @Mock
    List<String> list;


    @BeforeClass
    public void beforeClass() {
        MockitoAnnotations.initMocks(this);
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
