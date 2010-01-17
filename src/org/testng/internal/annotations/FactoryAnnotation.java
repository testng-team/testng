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
  
  public String getDataProvider() {
    return m_dataProvider;
  }

  public void setDataProvider(String dataProvider) {
    m_dataProvider = dataProvider;
  }

  public String[] getParameters() {
    return m_parameters;
  }
  
  public void setParameters(String[] parameters) {
    m_parameters = parameters;
  }

}
