package org.testng.internal;

import java.util.Iterator;

/**
 * A simple holder for parameters that contains the parameters and where these came from
 * (data provider or testng.xml)
 * @author cbeust
 *
 */
public class ParameterHolder {
  /**
   * Origin of the parameters.
   */
  public enum ParameterOrigin {
    ORIGIN_DATA_PROVIDER, // A data provider
    ORIGIN_XML // TestNG XML suite
  };

  public DataProviderHolder dataProviderHolder;
  public Iterator<Object[]> parameters;
  public ParameterOrigin origin;

  public ParameterHolder(Iterator<Object[]> parameters, ParameterOrigin origin, DataProviderHolder dph) {
    super();
    this.parameters = parameters;
    this.origin = origin;
    this.dataProviderHolder = dph;
  }

}
