package test.configuration.github1625;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
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
    assertThat(list).isNotNull();
  }

  @Test
  public void second() {
    assertThat(list).isNotNull();
  }
}
