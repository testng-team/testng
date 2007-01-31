package org.testng.internal.annotations;

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
  private Class m_class;
  
//  public DataProviderAnnotation(Class cls) {
//    m_class = cls;
//  }

  public String getName() {
    return m_name;
  }
  
  public void setName(String name) {
    m_name = name;
  }
  
//  public Class getDataProviderClass() {
//    return m_class;
//  }
  
}
