package org.testng.reporters.jq;

import java.util.List;
import org.testng.ISuite;

public abstract class BasePanel implements IPanel {
  public static final String C = "class";
  public static final String D = "div";
  public static final String S = "span";
  public static final String B = "button";
  public static final String I = "id";

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
    return Integer.toString(count)
        + " "
        + (count == 0 || count > 1
            ? (singular.endsWith("s") ? singular + "es" : singular + "s")
            : singular);
  }

  protected static String suiteToTag(ISuite suite) {
    return suite.getName().replaceAll("[^A-Za-z0-9]", "_");
  }
}
