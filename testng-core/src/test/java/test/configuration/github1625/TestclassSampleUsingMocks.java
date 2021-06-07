package test.configuration.github1625;

import java.util.List;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestclassSampleUsingMocks {
  @Mock List<String> list;

  AutoCloseable mocks;

  @BeforeClass
  public void beforeClass() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterClass
  public void closeMocks() throws Exception {
    mocks.close();
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
