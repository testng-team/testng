package org.testng.internal.annotations;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.Lazy;

/** An implementation of IFactory */
public class FactoryAnnotation extends BaseAnnotation implements IFactoryAnnotation {

  private String m_dataProvider = "";
  private @Nullable Class<?> m_dataProviderClass;
  private String m_dataProviderDynamicClass = "";
  private boolean m_enabled = true;
  private @Nullable List<Integer> m_indices;
  private Lazy m_lazy = Lazy.UNSET;

  @Override
  public String getDataProvider() {
    return m_dataProvider;
  }

  @Override
  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  @Override
  public void setDataProviderClass(@Nullable Class<?> dataProviderClass) {
    m_dataProviderClass = dataProviderClass;
  }

  @Override
  public @Nullable Class<?> getDataProviderClass() {
    return m_dataProviderClass;
  }

  @Override
  public String getDataProviderDynamicClass() {
    return m_dataProviderDynamicClass;
  }

  @Override
  public void setDataProviderDynamicClass(String v) {
    m_dataProviderDynamicClass = v;
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
  public @Nullable List<Integer> getIndices() {
    return m_indices;
  }

  @Override
  public void setIndices(List<Integer> indices) {
    m_indices = indices;
  }

  @Override
  public Lazy getLazy() {
    return m_lazy;
  }

  @Override
  public void setLazy(@Nullable Lazy lazy) {
    m_lazy = lazy == null ? Lazy.UNSET : lazy;
  }
}
