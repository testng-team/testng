package org.testng.annotations;

import org.testng.internal.annotations.IDataProvidable;

import java.util.List;

/**
 * Encapsulate the @Factory / @testng.factory annotation
 */
public interface IFactoryAnnotation extends IParameterizable, IDataProvidable {

  List<Integer> getIndices();

  void setIndices(List<Integer> indices);
}
