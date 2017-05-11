package org.testng;

import java.io.Serializable;
import java.util.List;


/**
 * @deprecated Not used
 */
@Deprecated
public interface IExtraOutput extends Serializable {

  /**
   * @return a List<String> representing the parameters passed
   * to this test method, or an empty List if no parameters were used.
   */
  public List<String> getParameterOutput();

}
