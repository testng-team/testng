package org.testng.xml;

public class XmlScript {

  private String language = null;
  private String expression = null;

  public void setLanguage(String language) {
    this.language = language;
  }

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
