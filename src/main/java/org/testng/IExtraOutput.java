package org.testng;

import java.io.Serializable;
import java.util.List;


/**
 * This class is used by Reporter to store the extra output to be later
 * included in the HTML report:
 * - User-generated report
 * - Parameter info
 *
 * Created on Feb 16, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IExtraOutput extends Serializable {

  /**
   * @return a List<String> representing the parameters passed
   * to this test method, or an empty List if no parameters were used.
   */
  public List<String> getParameterOutput();

}
