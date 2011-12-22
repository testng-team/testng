package org.testng.reporters.jq;

import org.testng.ISuite;

import java.util.List;

abstract public class BasePanel implements IPanel {
  public static final String C = "class";
  public static final String D = "div";
  public static final String S = "span";

  private Model m_model;

  public BasePanel(Model model) {
    m_model = model;
  }

  protected Model getModel() {
    return m_model;
  }

  protected List<ISuite> getSuites() {
    return getModel().getSuites();
  }
}
