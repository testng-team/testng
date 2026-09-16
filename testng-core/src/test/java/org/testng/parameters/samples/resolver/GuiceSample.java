package org.testng.parameters.samples.resolver;

import com.google.inject.name.Named;
import java.lang.reflect.Method;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

/**
 * Native injection, a Guice-bound service, a data provider value and a named generic binding.
 *
 * <p>{@code @Guice} is what makes TestNG build the suite's parent injector, from the module the
 * suite names; without it no injector exists for the resolver to ask.
 */
@Guice
public class GuiceSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"Linus"}};
  }

  @Test(dataProvider = "dp")
  public void test(
      Method current, GreetingService service, String name, @Named("names") List<String> names) {
    ParameterRecorder.record("test", current, service, name, names);
  }
}
