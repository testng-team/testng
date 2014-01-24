package org.testng.internal.annotations;

import org.testng.annotations.IFactoryAnnotation;

/**
 * An implementation of IFactory
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class FactoryAnnotation
  extends BaseAnnotation
  implements IFactoryAnnotation
{
  private String[] m_parameters = {};
  private String m_dataProvider = null;
  private Class<?> m_dataProviderClass;
  private boolean m_enabled = true;

  @Override
  public String getDataProvider() {
    return m_dataProvider;
  }

  @Override
  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  @Override
  public String[] getParameters() {
    return m_parameters;
  }

  public void setParameters(String[] parameters) {
    m_parameters = parameters;
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

}
