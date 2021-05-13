package test.dataprovider;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = StaticDataProviderSampleSample.InjectionProviderModule.class)
public class StaticDataProviderSampleSample {

  @Test(dataProvider = "static", dataProviderClass = StaticProvider.class)
  public void verifyStatic(String s) {}

  @Test(dataProvider = "external", dataProviderClass = NonStaticProvider.class)
  public void verifyExternal(String s) {}

  @Test(dataProvider = "injection", dataProviderClass = FieldInjectionProvider.class)
  public void verifyFieldInjection(String s) {}

  @Test(dataProvider = "injection", dataProviderClass = ConstructorInjectionProvider.class)
  public void verifyConstructorInjection(String s) {}

  public static class InjectionProviderModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(String.class).annotatedWith(Names.named("test")).toInstance("Cedric");
    }
  }
}
