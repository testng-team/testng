package org.testng.mustache;

import java.io.IOException;

public class IterableChunk extends BaseChunk {

  private Iterable m_iterable;
  private String m_template;
  private String m_variable;

  public IterableChunk(Model model, String template, Iterable iterable, String variable) {
    super(model);
    m_template = template;
    m_iterable = iterable;
    m_variable = variable;
  }

  @Override
  public String compose() {
    StringBuilder result = new StringBuilder();
    Mustache m = new Mustache();
    for (Object o : m_iterable) {
      m_model.push(m_variable, o);
      try {
        result.append(m.run(m_template, m_model));
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
      m_model.popSubModel();
    }
    return result.toString();
  }
}
