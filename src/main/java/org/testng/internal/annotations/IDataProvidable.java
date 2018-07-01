package org.testng.internal.annotations;

/**
 * A trait shared by all the annotations that have dataProvider/dataProviderClass attributes.
 */
public interface IDataProvidable {
  String getDataProvider();

  void setDataProvider(String v);

  Class<?> getDataProviderClass();

  void setDataProviderClass(Class<?> v);
}
