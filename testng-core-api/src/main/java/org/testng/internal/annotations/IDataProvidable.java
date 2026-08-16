package org.testng.internal.annotations;

import org.jspecify.annotations.Nullable;

/** A trait shared by all the annotations that have dataProvider/dataProviderClass attributes. */
public interface IDataProvidable {
  String getDataProvider();

  void setDataProvider(String v);

  /** @return The class holding the data provider, or {@code null} when none was named. */
  @Nullable
  Class<?> getDataProviderClass();

  void setDataProviderClass(@Nullable Class<?> v);

  String getDataProviderDynamicClass();

  void setDataProviderDynamicClass(String v);
}
