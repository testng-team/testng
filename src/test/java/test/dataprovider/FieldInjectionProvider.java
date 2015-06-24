package test.dataprovider;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.testng.annotations.DataProvider;

public class FieldInjectionProvider {

  @Inject @Named("test")
  private String value;

  @DataProvider(name = "injection")
  public Object[][] create() {
    return new Object[][] {
        new Object[] { value },
    };
  }
}
