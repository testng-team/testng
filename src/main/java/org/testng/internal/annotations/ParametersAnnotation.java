package org.testng.internal.annotations;

import org.testng.annotations.IParametersAnnotation;


/**
 * An implementation of IParameters
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ParametersAnnotation extends BaseAnnotation implements IParametersAnnotation {
  private String[] value = {};

  @Override
  public String[] getValue() {
    return value;
  }

  public void setValue(String[] value) {
    this.value = value;
  }

}
