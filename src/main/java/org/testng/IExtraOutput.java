package org.testng;

import java.util.List;

/**
 * @deprecated Not used
 */
@Deprecated
public interface IExtraOutput {

  /**
   * @return a List<String> representing the parameters passed
   * to this test method, or an empty List if no parameters were used.
   */
  List<String> getParameterOutput();

}
