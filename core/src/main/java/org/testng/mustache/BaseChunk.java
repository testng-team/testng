package org.testng.mustache;

public abstract class BaseChunk {

  protected Model m_model;

  public BaseChunk(Model model) {
    m_model = model;
  }

  protected void p(String string) {
    if (false) {
      System.out.println(string);
    }
  }

  abstract String compose();
}
