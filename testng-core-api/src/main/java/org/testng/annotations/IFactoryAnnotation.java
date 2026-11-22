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

  /**
   * Returns the lazy instantiation preference declared on the {@code @Factory}.
   *
   * <p>The default is {@link Lazy#UNSET}, which lets the suite or runner choose.
   *
   * @return the lazy instantiation preference
   */
  default Lazy getLazy() {
    return Lazy.UNSET;
  }

  /**
   * Sets the lazy instantiation preference.
   *
   * <p>The default implementation does nothing. Override this method to store a preference.
   *
   * @param lazy the preference, or {@code null} to leave it unset
   */
  default void setLazy(@Nullable Lazy lazy) {}
}
