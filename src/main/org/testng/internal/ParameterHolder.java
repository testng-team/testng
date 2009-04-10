package org.testng.internal;

import java.util.Iterator;

/**
 * A simple holder for parameters that contains the parameters and where these came from
 * (data provider or testng.xml)
 * @author cbeust
 *
 */
public class ParameterHolder {
  public static final int ORIGIN_DATA_PROVIDER = 1;
  public static final int ORIGIN_XML = 2;
  public static final int ORIGIN_NO_PARAMETERS = 2;

  public DataProviderHolder dataProviderHolder;
  public Iterator<Object[]> parameters;
  public int origin;

  public ParameterHolder(Iterator<Object[]> parameters, int origin, DataProviderHolder dph) {
    super();
    this.parameters = parameters;
    this.origin = origin;
    this.dataProviderHolder = dph;
  }

}
