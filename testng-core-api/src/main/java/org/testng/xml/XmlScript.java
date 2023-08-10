package org.testng.xml;

import org.checkerframework.checker.nullness.qual.Nullable;

public class XmlScript {

  private @Nullable String language = null;
  private @Nullable String expression = null;

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public @Nullable String getExpression() {
    return expression;
  }

  public @Nullable String getLanguage() {
    return language;
  }
}
