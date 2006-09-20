package org.testng.internal.annotations;


/**
 * An implementation of IParameters
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ParametersAnnotation 
  extends BaseAnnotation
  implements IParameters 
{
  private String[] m_value  = {};

  public String[] getValue() {
    return m_value;
  }
  
  public void setValue(String[] value) {
    m_value = value;
  }

}
