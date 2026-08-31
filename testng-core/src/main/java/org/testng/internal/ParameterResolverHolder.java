package org.testng.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.testng.IParameterResolver;
import org.testng.ListenerComparator;

/**
 * The {@link IParameterResolver}s registered for one suite.
 *
 * <p>It is deliberately not part of {@link org.testng.DataProviderHolder}: a resolver owns a
 * parameter whether or not the method has a data provider at all. One holder is created per {@link
 * org.testng.SuiteRunner} and shared, by reference, with its {@code TestRunner}s -- the same shape
 * the data provider listeners already have -- so a resolver registered while the suite is being set
 * up is seen by every {@code <test>} of that suite.
 */
public class ParameterResolverHolder {

  private final Map<Class<?>, IParameterResolver> resolvers = new ConcurrentHashMap<>();
  private final @Nullable ListenerComparator listenerComparator;

  public ParameterResolverHolder(IConfiguration configuration) {
    this.listenerComparator = Objects.requireNonNull(configuration).getListenerComparator();
  }

  public Collection<IParameterResolver> getResolvers() {
    return ListenerComparator.sort(resolvers.values(), listenerComparator);
  }

  public void addResolver(IParameterResolver resolver) {
    resolvers.putIfAbsent(resolver.getClass(), resolver);
  }

  public void addResolvers(Collection<IParameterResolver> toAdd) {
    toAdd.forEach(this::addResolver);
  }
}
