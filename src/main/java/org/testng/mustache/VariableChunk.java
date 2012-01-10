package org.testng.mustache;

public class VariableChunk extends BaseChunk {

  private String m_variable;

  public VariableChunk(Model model, String variable) {
    super(model);
    m_variable = variable;
  }

  @Override
  public String compose() {
    String result = m_model.resolveValueToString(m_variable);
    p("VariableChunk returning: " + result);
    return result;
  }

  @Override
  public String toString() {
    return "[VariableChunk " + m_variable + " model:" + m_model + "]";
  }
}
