package org.testng.annotations;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.internal.annotations.IDataProvidable;

/** Encapsulate the @Factory / @testng.factory annotation */
public interface IFactoryAnnotation extends IParameterizable, IDataProvidable {

  /**
   * @return - The indices to keep out of the data provider rows, or {@code null} when none were
   *     given.
   */
  @Nullable
  List<Integer> getIndices();

  void setIndices(List<Integer> indices);

  /** @return - The lazy instantiation preference declared on the {@code @Factory}. */
  Lazy getLazy();

  void setLazy(@Nullable Lazy lazy);
}
