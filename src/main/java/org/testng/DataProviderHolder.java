package org.testng;

import java.util.Collection;
import java.util.Collections;
import org.testng.collections.Sets;

/**
 * A holder class that is aimed at acting as a container for holding various different aspects of a
 * data provider such as listeners/interceptors etc.,
 */
public class DataProviderHolder {

  private final Collection<IDataProviderListener> listeners = Sets.newHashSet();
  private final Collection<IDataProviderInterceptor> interceptors = Sets.newHashSet();

  public Collection<IDataProviderListener> getListeners() {
    return Collections.unmodifiableCollection(listeners);
  }

  public Collection<IDataProviderInterceptor> getInterceptors() {
    return Collections.unmodifiableCollection(interceptors);
  }

  public void addListeners(Collection<IDataProviderListener> listeners) {
    listeners.forEach(this::addListener);
  }

  public void addListener(IDataProviderListener listener) {
    listeners.add(listener);
  }

  public void addInterceptors(Collection<IDataProviderInterceptor> interceptors) {
    interceptors.forEach(this::addInterceptor);
  }

  public void addInterceptor(IDataProviderInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public void merge(DataProviderHolder other) {
    this.listeners.addAll(other.getListeners());
    this.interceptors.addAll(other.getInterceptors());
  }
}
