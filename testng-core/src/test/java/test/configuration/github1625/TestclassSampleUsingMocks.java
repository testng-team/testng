package test.configuration.github1625;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    assertThat(list).isNotNull();
  }

  @Test
  public void second() {
    assertThat(list).isNotNull();
  }
}
