package org.testng;

import static org.testng.ListenerComparator.sort;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.testng.internal.IConfiguration;

/**
 * Holds the {@link IDataProviderListener} listeners and the {@link IDataProviderInterceptor}
 * interceptors that TestNG calls for data providers.
 */
public class DataProviderHolder {

  private final Map<Class<?>, IDataProviderListener> listeners = new ConcurrentHashMap<>();
  private final Collection<IDataProviderInterceptor> interceptors = new HashSet<>();
  private final @Nullable ListenerComparator listenerComparator;

  /**
   * Creates an empty holder.
   *
   * @param configuration the configuration that gives the listener comparator. The holder sorts the
   *     listeners and the interceptors with it.
   */
  public DataProviderHolder(IConfiguration configuration) {
    this.listenerComparator = Objects.requireNonNull(configuration).getListenerComparator();
  }

  /**
   * Returns the data provider listeners.
   *
   * @return the listeners, in the order of the listener comparator when there is one. You cannot
   *     change the returned collection.
   */
  public Collection<IDataProviderListener> getListeners() {
    return sort(listeners.values(), listenerComparator);
  }

  /**
   * Returns the data provider interceptors.
   *
   * @return the interceptors, in the order of the listener comparator when there is one. You cannot
   *     change the returned collection.
   */
  public Collection<IDataProviderInterceptor> getInterceptors() {
    return sort(interceptors, listenerComparator);
  }

  /**
   * Adds each listener in a collection, as {@link #addListener(IDataProviderListener)} does.
   *
   * @param listeners the listeners to add.
   */
  public void addListeners(Collection<IDataProviderListener> listeners) {
    listeners.forEach(this::addListener);
  }

  /**
   * Adds a listener, unless this holder already has a listener of the same class.
   *
   * @param listener the listener to add.
   */
  public void addListener(IDataProviderListener listener) {
    listeners.putIfAbsent(listener.getClass(), listener);
  }

  /**
   * Adds each interceptor in a collection.
   *
   * @param interceptors the interceptors to add.
   */
  public void addInterceptors(Collection<IDataProviderInterceptor> interceptors) {
    interceptors.forEach(this::addInterceptor);
  }

  /**
   * Adds an interceptor. When you add the same interceptor twice, this holder keeps one copy.
   *
   * @param interceptor the interceptor to add.
   */
  public void addInterceptor(IDataProviderInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  /**
   * Adds the listeners and the interceptors of another holder to this holder.
   *
   * <p>This holder skips a listener when it already has a listener of the same class.
   *
   * @param other the holder to copy from.
   */
  public void merge(DataProviderHolder other) {
    addListeners(other.getListeners());
    this.interceptors.addAll(other.getInterceptors());
  }
}
