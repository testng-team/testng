package org.testng.mustache;

public class StringChunk extends BaseChunk {

  private String m_string;

  public StringChunk(Model model, String string) {
    super(model);
    m_string = string;
  }

  @Override
  public String compose() {
    return m_string;
  }

  @Override
  public String toString() {
    return "[StringChunk " + m_string + "]";
  }
}
