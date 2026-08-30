package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.collections.Maps;
import org.testng.collections.SetMultiMap;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

public class IgnoredMethodsPanel extends BaseMultiSuitePanel {

  public IgnoredMethodsPanel(Model model) {
    super(model);
  }

  @Override
  public String getPrefix() {
    return "ignored-methods-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return pluralize(suite.getExcludedMethods().size(), "ignored method");
  }

  @Override
  void writeContent(ISuite suite, XMLStringBuffer xsb) {
    SetMultiMap<Class<?>, ITestNGMethod> map = Maps.newSetMultiMap();

    for (ITestNGMethod method : suite.getExcludedMethods()) {
      map.put(Utils.requireTestClassOf(method).getRealClass(), method);
    }

    for (Class<?> c : map.keySet()) {
      xsb.push(D, C, "ignored-class-div");
      xsb.addRequired(S, c.getName(), C, "ignored-class-name");
      xsb.push(D, C, "ignored-methods-div");
      for (ITestNGMethod m : map.get(c)) {
        xsb.addRequired(S, m.getMethodName(), C, "ignored-method-name");
        xsb.addEmptyElement("br");
      }
      xsb.pop(D);
      xsb.pop(D);
    }
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return "Ignored methods";
  }
}
