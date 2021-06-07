package org.testng.internal.invokers;

import java.util.Iterator;
import org.testng.IDataProviderMethod;

/**
 * A simple holder for parameters that contains the parameters and where these came from (data
 * provider or testng.xml)
 */
public class ParameterHolder {
  /** Origin of the parameters. */
  public enum ParameterOrigin {
    ORIGIN_DATA_PROVIDER, // A data provider
    ORIGIN_XML, // TestNG XML suite
    NATIVE // Native injection is involved.
  }

  final IDataProviderMethod dataProviderHolder;
  public final Iterator<Object[]> parameters;
  final ParameterOrigin origin;

  public ParameterHolder(
      Iterator<Object[]> parameters, ParameterOrigin origin, IDataProviderMethod dph) {
    super();
    this.parameters = parameters;
    this.origin = origin;
    this.dataProviderHolder = dph;
  }
}
