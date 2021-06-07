package test.configuration.github1625;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
