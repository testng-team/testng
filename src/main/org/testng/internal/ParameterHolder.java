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

  public ParameterHolder(Iterator<Object[]> parameters, int origin) {
    super();
    this.parameters = parameters;
    this.origin = origin;
  }

  public Iterator<Object[]> parameters;
  
  public int origin;
}
