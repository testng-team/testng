package org.testng.internal.annotations;

import org.testng.annotations.IFactoryAnnotation;

import java.util.List;

/**
 * An implementation of IFactory
 */
public class FactoryAnnotation extends BaseAnnotation implements IFactoryAnnotation {

  private String m_dataProvider = null;
  private Class<?> m_dataProviderClass;
  private boolean m_enabled = true;
  private List<Integer> m_indices;

  @Override
  public String getDataProvider() {
    return m_dataProvider;
  }

  @Override
  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  public void setDataProviderClass(Class<?> dataProviderClass) {
    m_dataProviderClass = dataProviderClass;
  }

  @Override
  public Class<?> getDataProviderClass() {
    return m_dataProviderClass;
  }

  @Override
  public boolean getEnabled() {
    return m_enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public List<Integer> getIndices() {
    return m_indices;
  }

  @Override
  public void setIndices(List<Integer> indices) {
    m_indices = indices;
  }
}
