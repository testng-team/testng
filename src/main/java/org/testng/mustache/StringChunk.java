package org.testng.mustache;

public class StringChunk extends BaseChunk {

  private String m_string;

  public StringChunk(Model model, String string) {
    super(model);
    m_string = string;
  }

  @Override
  String compose() {
    return m_string;
  }
}
