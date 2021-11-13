package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.collections.Maps;

class ScriptMethodSelector {

  private final ScriptEngine engine;
  private final String expression;

  ScriptMethodSelector(ScriptEngine engine, String expression) {
    this.engine = engine;
    this.expression = expression.trim();
  }

  boolean includeMethodFromExpression(ITestNGMethod tm) {
    Map<String, String> groups = Maps.newHashMap();
    for (String group : tm.getGroups()) {
      groups.put(group, group);
    }
    try {
      setContext(engine, groups, tm);
      Object evalResult = engine.eval(expression);
      if (evalResult == null) {
        String msg =
            String.format(
                "The "
                    + engine.getFactory().getLanguageName()
                    + " expression [%s] evaluated to null.",
                expression);
        throw new TestNGException(msg);
      }
      return (Boolean) evalResult;
    } catch (ScriptException e) {
      throw new TestNGException(e);
    } finally {
      resetContext(engine);
    }
  }

  private static void setContext(
      ScriptEngine engine, Map<String, String> groups, ITestNGMethod tm) {
    ScriptContext context = engine.getContext();
    Method method = tm.getConstructorOrMethod().getMethod();
    context.setAttribute("method", method, ScriptContext.ENGINE_SCOPE);
    context.setAttribute("groups", groups, ScriptContext.ENGINE_SCOPE);
    context.setAttribute("testngMethod", tm, ScriptContext.ENGINE_SCOPE);
  }

  private static void resetContext(ScriptEngine engine) {
    ScriptContext context = engine.getContext();
    context.removeAttribute("method", ScriptContext.ENGINE_SCOPE);
    context.removeAttribute("groups", ScriptContext.ENGINE_SCOPE);
    context.removeAttribute("testngMethod", ScriptContext.ENGINE_SCOPE);
  }
}
