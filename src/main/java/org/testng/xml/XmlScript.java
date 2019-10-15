package org.testng.xml;

import org.testng.xml.dom.TagContent;

public class XmlScript {

  private String language;
  private String expression;

  public void setLanguage(String language) {
    this.language = language;
  }

  @TagContent(name = "script")
  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

  public String getLanguage() {
    return language;
  }
}
