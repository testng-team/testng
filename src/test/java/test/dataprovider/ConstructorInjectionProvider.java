package test.dataprovider;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.testng.annotations.DataProvider;

public class ConstructorInjectionProvider {

  private final String value;

  @Inject
  public ConstructorInjectionProvider(@Named("test") String value) {
    this.value = value;
  }

  @DataProvider(name = "injection")
  public Object[][] create() {
    return new Object[][] {
        new Object[] { value },
    };
  }
}
