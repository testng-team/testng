package org.testng.internal;

import java.util.Iterator;
import org.testng.IDataProviderMethod;

/**
 * A simple holder for parameters that contains the parameters and where these came from (data
 * provider or testng.xml)
 *
 * @author cbeust
 */
public class ParameterHolder {

  final IDataProviderMethod dataProviderHolder;
  final Iterator<Object[]> parameters;
  final ParameterOrigin origin;
  public ParameterHolder(
      Iterator<Object[]> parameters, ParameterOrigin origin, IDataProviderMethod dph) {
    super();
    this.parameters = parameters;
    this.origin = origin;
    this.dataProviderHolder = dph;
  }

  /**
   * Origin of the parameters.
   */
  public enum ParameterOrigin {
    ORIGIN_DATA_PROVIDER, // A data provider
    ORIGIN_XML, // TestNG XML suite
    NATIVE // Native injection is involved.
  }
}
