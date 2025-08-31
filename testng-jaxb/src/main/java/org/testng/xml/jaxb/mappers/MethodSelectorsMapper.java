package org.testng.xml.jaxb.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.jaxb.MethodSelectors;
import org.testng.xml.jaxb.Script;
import org.testng.xml.jaxb.SelectorClass;

public class MethodSelectorsMapper {

  public static List<XmlMethodSelector> toXmlMethodSelectors(MethodSelectors methodSelectors) {
    return methodSelectors.getMethodSelector().stream()
        .map(
            ms -> {
              XmlMethodSelector xmlMethodSelector = new XmlMethodSelector();
              if (ms.getScript() != null) {
                Script script = ms.getScript();
                xmlMethodSelector.setScript(ScriptMapper.toXmlScript(script));
              } else if (ms.getSelectorClass() != null && !ms.getSelectorClass().isEmpty()) {
                SelectorClass sc = ms.getSelectorClass().get(0);
                xmlMethodSelector.setName(sc.getName());
                xmlMethodSelector.setPriority(Integer.parseInt(sc.getPriority()));
              }
              return xmlMethodSelector;
            })
        .collect(Collectors.toList());
  }
}
