package org.testng.internal;

import java.util.ArrayList;
import java.util.List;

import org.testng.IExtraOutput;

/**
 * This class is used by Reporter to store the extra output to be later
 * included in the HTML report:
 * - User-generated report
 * - Parameter info
 * 
 * Created on Feb 16, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ExtraOutput implements IExtraOutput {
  private List<String> m_parameterOutput = new ArrayList<String>();
  
  public List<String> getParameterOutput() {
    return m_parameterOutput;
  }
}
