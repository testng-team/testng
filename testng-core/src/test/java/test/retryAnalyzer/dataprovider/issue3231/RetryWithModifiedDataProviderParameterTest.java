package test.retryAnalyzer.dataprovider.issue3231;

import java.util.Objects;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RetryWithModifiedDataProviderParameterTest extends SimpleBaseTest {

  @Test(retryAnalyzer = RetryAnalyzer.class, dataProvider = "dpNewObject")
  public void willNotStopAfter3Failures(CustomType customType) {
    customType.setId(UUID.randomUUID());
    Assert.fail("Kaboom!");
  }

  @DataProvider
  public CustomType[] dpNewObject() {
    return new CustomType[] {new CustomType()};
  }

  public static class CustomType {
    private UUID id;

    public void setId(UUID id) {
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      CustomType custom = (CustomType) o;
      return Objects.equals(id, custom.id);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id);
    }
  }
}
