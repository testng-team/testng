package org.testng.reporters.jq;

abstract public class BasePanel implements IPanel {

  private Model m_model;

  public BasePanel(Model model) {
    m_model = model;
  }

  protected Model getModel() {
    return m_model;
  }
}
