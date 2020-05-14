package org.testng.reporters.jq;

import org.testng.ISuite;
import org.testng.reporters.XMLStringBuffer;

import java.util.Collections;
import java.util.List;

public class GroupPanel extends BaseMultiSuitePanel {
  public GroupPanel(Model model) {
    super(model);
  }

  @Override
  public String getPrefix() {
    return "group-";
  }

  @Override
  public String getHeader(ISuite suite) {
    return "Groups for " + suite.getName();
  }

  @Override
  public String getContent(ISuite suite, XMLStringBuffer main) {
    XMLStringBuffer xsb = new XMLStringBuffer(main.getCurrentIndent());
    List<String> sortedGroups = getModel().getGroups(suite.getName());
    Collections.sort(sortedGroups);
    for (String group : sortedGroups) {
      xsb.push(D, C, "test-group");
      xsb.addRequired(S, group, C, "test-group-name");
      xsb.addEmptyElement("br");
      List<String> sortedMethods = getModel().getMethodsInGroup(group);
      for (String method : sortedMethods) {
        xsb.push(D, C, "method-in-group");
        xsb.addRequired(S, method, C, "method-in-group-name");
        xsb.addEmptyElement("br");
        xsb.pop(D);
      }
      xsb.pop(D);
    }
    return xsb.toXML();
  }

  @Override
  public String getNavigatorLink(ISuite suite) {
    return pluralize(getModel().getGroups(suite.getName()).size(), "group");
  }
}
