package org.testng.xml;

import org.jspecify.annotations.Nullable;

public class XmlScript {

  private @Nullable String language;
  private @Nullable String expression;

  public void setLanguage(@Nullable String language) {
    this.language = language;
  }

  public void setExpression(@Nullable String expression) {
    this.expression = expression;
  }

  public @Nullable String getExpression() {
    return expression;
  }

  public @Nullable String getLanguage() {
    return language;
  }
}
