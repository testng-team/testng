package org.testng.mustache;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Stack;

public class Model {
  private Map<String, Object> m_model;
  private Stack<SubModel> m_subModels = new Stack<>();
  private static class SubModel {
    String variable;
    Object subModel;
  }

  public Model(Map<String, Object> model) {
    m_model = model;
  }

  public void push(String variable, Object subModel) {
    SubModel sl = new SubModel();
    sl.variable = variable;
    sl.subModel = subModel;
    m_subModels.push(sl);
  }

  public Value resolveValue(String variable) {
    if (! m_subModels.isEmpty()) {
      for (SubModel object : m_subModels) {
        Value value = resolveOnClass(object.subModel, variable);
        if (value != null) {
          return value;
        }
      }
    }

    return new Value(m_model.get(variable));
  }

  private Value resolveOnClass(Object object, String variable) {
//    if (object instanceof Iterable) {
//      Iterable it = (Iterable) object;
//      List<Object> result = Lists.newArrayList();
//      for (Object o : it) {
//        List<Object> values = resolveOnClass(o, variable);
//        result.addAll(values);
//      }
//      return result;
//    } else {
      Class<? extends Object> cls = object.getClass();
      try {
        Field f = cls.getField(variable);
        return new Value(f.get(object));
      } catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
//        e.printStackTrace();
      }
//    }

    return null;
  }

  public Object getTopSubModel() {
    return m_subModels.peek().variable;
  }

  public void popSubModel() {
    m_subModels.pop();
  }

  public String resolveValueToString(String variable) {
    StringBuilder result = new StringBuilder();
    Value value = resolveValue(variable);
    if (value.get() != null) {
      return value.get().toString();
    } else {
      return "";
    }
  }

  @Override
  public String toString() {
    return "[Model " + m_model + " subModel:" + m_subModels + "]";
  }
}
