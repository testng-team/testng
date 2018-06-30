package org.testng.xml;

import org.testng.xml.dom.TagContent;

public class XmlScript {

  private String language;
  private String expression;

  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @deprecated Use {@link #setExpression(String)} instead
   */
  @Deprecated
  public void setScript(String script) {
    expression = script;
  }

  @TagContent(name = "script")
  public void setExpression(String expression) {
    this.expression = expression;
  }

  /**
   * @deprecated User {@link #getExpression()} instead
   */
  @Deprecated
  public String getScript() {
    return expression;
  }

  public String getExpression() {
    return expression;
  }

  public String getLanguage() {
    return language;
  }
}
