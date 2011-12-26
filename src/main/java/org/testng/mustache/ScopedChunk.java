package org.testng.mustache;

import java.io.IOException;

public class ScopedChunk extends BaseChunk {

  private String m_template;
  private Object m_object;
  private String m_variable;

  public ScopedChunk(Model model, String template, Object object, String variable) {
    super(model);
    m_template = template;
    m_object = object;
    m_variable = variable;
  }

  @Override
  String compose() {
    StringBuilder result = new StringBuilder();
    Mustache m = new Mustache();
    m_model.push(m_variable, m_object);
    try {
      result.append(m.run(m_template, m_model));
      m_model.popSubModel();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }

    p("Scoped chunk returning: " + result.toString());
    return result.toString();
  }

}
