package org.testng.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.testng.IParameterResolver;
import org.testng.ListenerComparator;
import org.testng.log4testng.Logger;

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

  private static final Logger LOGGER = Logger.getLogger(ParameterResolverHolder.class);

  private final Map<Class<?>, IParameterResolver> resolvers = new ConcurrentHashMap<>();
  private final @Nullable ListenerComparator listenerComparator;

  public ParameterResolverHolder(IConfiguration configuration) {
    this.listenerComparator = Objects.requireNonNull(configuration).getListenerComparator();
  }

  public Collection<IParameterResolver> getResolvers() {
    return ListenerComparator.sort(resolvers.values(), listenerComparator);
  }

  /**
   * One resolver per class, the first to arrive. The same instance registered twice -- once by the
   * suite and once by a runner, say -- is the case this is for. A second, differently configured
   * instance of one class is the case it cannot serve, and it is logged rather than dropped without
   * a word, the way {@code TestNG.addListener} logs a duplicate listener.
   */
  @SuppressWarnings("ReferenceEquality") // the same instance twice is the case to stay quiet on
  public void addResolver(IParameterResolver resolver) {
    IParameterResolver kept = resolvers.putIfAbsent(resolver.getClass(), resolver);
    if (kept != null && kept != resolver) {
      LOGGER.warn(
          "Ignoring a second instance of parameter resolver "
              + resolver.getClass().getName()
              + ": resolvers are registered once per class, and the first instance is kept");
    }
  }

  public void addResolvers(Collection<IParameterResolver> toAdd) {
    toAdd.forEach(this::addResolver);
  }
}
