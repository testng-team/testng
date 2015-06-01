package org.testng.internal;

import bsh.EvalError;
import bsh.Interpreter;

import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.collections.Maps;

import java.lang.reflect.Method;
import java.util.Map;

public class Bsh implements IBsh {
  private static Interpreter s_interpreter;

  @Override
  public boolean includeMethodFromExpression(String expression, ITestNGMethod tm) {
    boolean result = false;

    Interpreter interpreter = getInterpreter();
    try {
      Map<String, String> groups = Maps.newHashMap();
      for (String group : tm.getGroups()) {
        groups.put(group, group);
      }
      setContext(interpreter, tm.getMethod(), groups, tm);
      Object evalResult = interpreter.eval(expression);
      result = (Boolean) evalResult;
    }
    catch (EvalError evalError) {
      Utils.log("bsh.Interpreter", 2, "Cannot evaluate expression:"
          + expression + ":" + evalError.getMessage());
    }
    finally {
      resetContext(interpreter);
    }

    return result;

  }

  private static Interpreter getInterpreter() {
    if(null == s_interpreter) {
      s_interpreter= new Interpreter();
    }

    return s_interpreter;
  }

  private void setContext(Interpreter interpreter, Method method, Map<String, String> groups, ITestNGMethod tm) {
    try {
      interpreter.set("method", method);
      interpreter.set("groups", groups);
      interpreter.set("testngMethod", tm);
    }
    catch(EvalError evalError) {
      throw new TestNGException("Cannot set BSH interpreter", evalError);
    }
  }

  private void resetContext(Interpreter interpreter) {
    try {
      interpreter.unset("method");
      interpreter.unset("groups");
      interpreter.unset("testngMethod");
    }
    catch(EvalError evalError) {
      Utils.log("bsh.Interpreter", 2, "Cannot reset interpreter:" + evalError.getMessage());
    }
  }

}
