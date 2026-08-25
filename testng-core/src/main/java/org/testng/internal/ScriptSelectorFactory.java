package org.testng.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import javax.script.ScriptEngineFactory;
import org.testng.TestNGException;
import org.testng.xml.XmlScript;

public final class ScriptSelectorFactory {

  private static final Map<String, ScriptEngineFactory> ENGINE_FACTORIES = new HashMap<>();

  private ScriptSelectorFactory() {}

  public static ScriptMethodSelector getScriptSelector(XmlScript script) {

    if (script == null || script.getLanguage() == null) {
      throw new IllegalArgumentException("Language name must not be null");
    }

    String languageName = script.getLanguage().toLowerCase(Locale.ROOT);
    ScriptEngineFactory engineFactory = ENGINE_FACTORIES.get(languageName);
    if (engineFactory == null) {
      ServiceLoader<ScriptEngineFactory> loader = ServiceLoader.load(ScriptEngineFactory.class);
      for (ScriptEngineFactory factory : loader) {
        ENGINE_FACTORIES.put(factory.getLanguageName().toLowerCase(Locale.ROOT), factory);
      }

      engineFactory = ENGINE_FACTORIES.get(languageName);
    }

    if (engineFactory == null) {
      throw new TestNGException(
          "No engine found for language: "
              + script.getLanguage()
              + ". "
              + "Please check your dependencies and have a look at "
              + "https://github.com/cbeust/testng/wiki/Supported-script-engines");
    }

    return new ScriptMethodSelector(
        engineFactory.getScriptEngine(),
        Objects.requireNonNull(script.getExpression(), "the <script> tag carries no expression"));
  }
}
