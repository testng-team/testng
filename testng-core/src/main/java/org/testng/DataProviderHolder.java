package org.testng;

import static org.testng.ListenerComparator.*;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
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
  private final ListenerComparator listenerComparator;

  public DataProviderHolder(IConfiguration configuration) {
    this.listenerComparator = Objects.requireNonNull(configuration).getListenerComparator();
  }

  public Collection<IDataProviderListener> getListeners() {
    return sort(listeners.values(), listenerComparator);
  }

  public Collection<IDataProviderInterceptor> getInterceptors() {
    return sort(interceptors, listenerComparator);
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
