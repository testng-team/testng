package org.testng.internal.annotations;

import org.testng.annotations.IExpectedExceptionsAnnotation;

/**
 * An implementation of IExpectedExceptions
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ExpectedExceptionsAnnotation extends BaseAnnotation implements IExpectedExceptionsAnnotation {
  private Class[] value = {};

  @Override
  public Class[] getValue() {
    return value;
  }

  public void setValue(Class[] value) {
    this.value = value;
  }
}
