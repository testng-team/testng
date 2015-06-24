package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.collections.Maps;
import org.testng.collections.SetMultiMap;
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
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    SetMultiMap<Class<?>, ITestNGMethod> map = Maps.newSetMultiMap();

    for (ITestNGMethod method : suite.getExcludedMethods()) {
      map.put(method.getTestClass().getRealClass(), method);
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
    return xsb.toXML();
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return "Ignored methods";
  }

}
