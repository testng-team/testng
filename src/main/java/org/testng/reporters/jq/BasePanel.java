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

  protected static String pluralize(int count, String singular) {
    return Integer.toString(count) + " " + (count == 0 || count > 1
        ? (singular.endsWith("s") ? singular + "es" : singular + "s")
        : singular);
  }

  protected static String suiteToTag(ISuite suite) {
    return suite.getName().replace(" ", "_");
  }

}
