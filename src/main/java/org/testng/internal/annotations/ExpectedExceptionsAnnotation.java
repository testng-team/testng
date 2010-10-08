package org.testng.internal.annotations;

import org.testng.annotations.IExpectedExceptionsAnnotation;

/**
 * An implementation of IExpectedExceptions
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ExpectedExceptionsAnnotation
  extends BaseAnnotation
  implements IExpectedExceptionsAnnotation
{
  private Class[] m_value = {};

  @Override
  public Class[] getValue() {
    return m_value;
  }

  public void setValue(Class[] value) {
    m_value = value;
  }
}
