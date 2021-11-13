package org.testng.annotations;

import java.util.List;

/** Encapsulate the @DataProvider / @testng.data-provider annotation */
public interface IDataProviderAnnotation extends IAnnotation {
  /** @return The name of this DataProvider. */
  String getName();

  void setName(String name);

  /**
   * Whether this data provider should be used in parallel.
   *
   * @return true if in parallel
   */
  boolean isParallel();

  void setParallel(boolean parallel);

  List<Integer> getIndices();

  void setIndices(List<Integer> indices);
}
