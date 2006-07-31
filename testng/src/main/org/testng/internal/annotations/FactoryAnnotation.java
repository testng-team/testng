package org.testng.internal.annotations;

/**
 * An implementation of IFactory
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class FactoryAnnotation implements IFactory {
  private String[] m_parameters = {};
  
  public String[] getParameters() {
    return m_parameters;
  }
  
  public void setParameters(String[] parameters) {
    m_parameters = parameters;
  }

}
