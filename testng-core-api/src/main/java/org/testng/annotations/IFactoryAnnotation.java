package org.testng.annotations;

import java.util.List;
import org.testng.internal.annotations.IDataProvidable;

/** Encapsulate the @Factory / @testng.factory annotation */
public interface IFactoryAnnotation extends IParameterizable, IDataProvidable {

  List<Integer> getIndices();

  void setIndices(List<Integer> indices);

  /** @return - The lazy instantiation preference declared on the {@code @Factory}. */
  Lazy getLazy();

  void setLazy(Lazy lazy);
}
