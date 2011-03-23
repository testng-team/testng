package org.testng.internal.annotations;

/**
 * A trait shared by all the annotations that have dataProvider/dataProviderClass attributes.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public interface IDataProvidable {
  public String getDataProvider();
  public void setDataProvider(String v);

  public Class<?> getDataProviderClass();
  public void setDataProviderClass(Class<?> v);
}
