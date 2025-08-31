package org.testng.xml.jaxb.mappers;

import java.util.stream.Collectors;
import org.testng.xml.XmlScript;
import org.testng.xml.jaxb.Script;

public class ScriptMapper {

  public static XmlScript toXmlScript(Script script) {
    XmlScript xmlScript = new XmlScript();
    xmlScript.setLanguage(script.getLanguage());
    xmlScript.setExpression(
        script.getContent().stream().map(Object::toString).collect(Collectors.joining()));
    return xmlScript;
  }
}
