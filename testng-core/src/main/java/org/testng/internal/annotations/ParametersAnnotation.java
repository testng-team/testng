package org.testng.internal.annotations;

import org.testng.annotations.IParametersAnnotation;

/** An implementation of IParameters */
public class ParametersAnnotation extends BaseAnnotation implements IParametersAnnotation {

  private String[] m_value = {};

  @Override
  public String[] getValue() {
    return m_value;
  }

  public void setValue(String[] value) {
    m_value = value;
  }
}
