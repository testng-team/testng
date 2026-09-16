package org.testng.parameters.samples.resolver;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.util.Arrays;
import java.util.List;

/** Binds a service by type and a {@code List<String>} by name, so both lookups are exercised. */
public class GuiceResolverModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(GreetingService.class).toInstance(name -> "hello " + name);
    bind(new TypeLiteral<List<String>>() {})
        .annotatedWith(Names.named("names"))
        .toInstance(Arrays.asList("Ada", "Grace"));
  }
}
