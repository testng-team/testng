package org.testng.internal;

import org.testng.IExtraOutput;
import org.testng.collections.Lists;

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
public class ExtraOutput implements IExtraOutput {
  /**
   *
   */
  private static final long serialVersionUID = 8195388419611912192L;
  private List<String> m_parameterOutput = Lists.newArrayList();

  @Override
  public List<String> getParameterOutput() {
    return m_parameterOutput;
  }
}
