package org.testng.internal.annotations;

import org.testng.annotations.IDataProvider;

/**
 * An implementation of IDataProvider.
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class DataProviderAnnotation 
  extends BaseAnnotation
  implements IDataProvider 
{
  private String m_name;

  public String getName() {
    return m_name;
  }
  
  public void setName(String name) {
    m_name = name;
  }
}
