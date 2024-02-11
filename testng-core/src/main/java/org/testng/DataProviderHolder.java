package org.testng;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.IConfiguration;

/**
 * A holder class that is aimed at acting as a container for holding various different aspects of a
 * data provider such as listeners/interceptors etc.,
 */
public class DataProviderHolder {

  private final Map<Class<?>, IDataProviderListener> listeners = Maps.newConcurrentMap();
  private final Collection<IDataProviderInterceptor> interceptors = Sets.newHashSet();

  private final IConfiguration configuration;

  public DataProviderHolder(IConfiguration configuration) {
    this.configuration = configuration;
  }

  public Collection<IDataProviderListener> getListeners() {
    List<IDataProviderListener> original = Lists.newArrayList(listeners.values());
    ListenerComparator comparator = getConfiguration().getListenerComparator();
    if (comparator != null) {
      original.sort(comparator::compare);
    }
    return Collections.unmodifiableCollection(original);
  }

  public IConfiguration getConfiguration() {
    return Objects.requireNonNull(configuration);
  }

  public Collection<IDataProviderInterceptor> getInterceptors() {
    List<IDataProviderInterceptor> original = Lists.newArrayList(interceptors);
    ListenerComparator comparator = getConfiguration().getListenerComparator();
    if (comparator != null) {
      original.sort(comparator::compare);
    }
    return Collections.unmodifiableCollection(original);
  }

  public void addListeners(Collection<IDataProviderListener> listeners) {
    listeners.forEach(this::addListener);
  }

  public void addListener(IDataProviderListener listener) {
    listeners.putIfAbsent(listener.getClass(), listener);
  }

  public void addInterceptors(Collection<IDataProviderInterceptor> interceptors) {
    interceptors.forEach(this::addInterceptor);
  }

  public void addInterceptor(IDataProviderInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public void merge(DataProviderHolder other) {
    addListeners(other.getListeners());
    this.interceptors.addAll(other.getInterceptors());
  }
}
