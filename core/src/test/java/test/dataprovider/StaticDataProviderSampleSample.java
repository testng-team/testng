package test.dataprovider;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Guice(modules = StaticDataProviderSampleSample.InjectionProviderModule.class)
public class StaticDataProviderSampleSample {

  @Test(dataProvider = "static", dataProviderClass = StaticProvider.class)
  public void verifyStatic(String s) {
    assertEquals(s, "Cedric");
  }

  @Test(dataProvider = "external", dataProviderClass = NonStaticProvider.class)
  public void verifyExternal(String s) {
    assertEquals(s, "Cedric");
  }

  @Test(dataProvider = "injection", dataProviderClass = FieldInjectionProvider.class)
  public void verifyFieldInjection(String s) {
    assertEquals(s, "Cedric");
  }

  @Test(dataProvider = "injection", dataProviderClass = ConstructorInjectionProvider.class)
  public void verifyConstructorInjection(String s) {
    assertEquals(s, "Cedric");
  }

  public static class InjectionProviderModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(String.class).annotatedWith(Names.named("test")).toInstance("Cedric");
    }
  }
}
