package org.testng.internal;

import org.testng.*;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;

import java.util.List;
import java.util.Map;

public class Configuration implements IConfiguration {

  private IAnnotationFinder m_annotationFinder;
  private ITestObjectFactory m_objectFactory;
  private IHookable m_hookable;
  private IConfigurable m_configurable;
  private final Map<Class<? extends IExecutionListener>, IExecutionListener> m_executionListeners = Maps.newHashMap();
  private final Map<Class<? extends IConfigurationListener>, IConfigurationListener> m_configurationListeners = Maps.newHashMap();

  public Configuration() {
    init(new JDK15AnnotationFinder(new DefaultAnnotationTransformer()));
  }

  public Configuration(IAnnotationFinder finder) {
    init(finder);
  }

  private void init(IAnnotationFinder finder) {
    m_annotationFinder = finder;
  }

  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  @Override
  public void setAnnotationFinder(IAnnotationFinder finder) {
    m_annotationFinder = finder;
  }

  @Override
  public ITestObjectFactory getObjectFactory() {
    return m_objectFactory;
  }

  @Override
  public void setObjectFactory(ITestObjectFactory factory) {
    m_objectFactory = factory;
  }

  @Override
  public IHookable getHookable() {
    return m_hookable;
  }

  @Override
  public void setHookable(IHookable h) {
    m_hookable = h;
  }

  @Override
  public IConfigurable getConfigurable() {
    return m_configurable;
  }

  @Override
  public void setConfigurable(IConfigurable c) {
    m_configurable = c;
  }

  @Override
  public List<IExecutionListener> getExecutionListeners() {
    return Lists.newArrayList(m_executionListeners.values());
  }

  @Override
  public void addExecutionListener(IExecutionListener l) {
    if (!m_executionListeners.keySet().contains(l.getClass())) {
        m_executionListeners.put(l.getClass(), l);
    }
  }

  @Override
  public List<IConfigurationListener> getConfigurationListeners() {
    return Lists.newArrayList(m_configurationListeners.values());
  }

  @Override
  public void addConfigurationListener(IConfigurationListener cl) {
    m_configurationListeners.put(cl.getClass(), cl);
  }
}
