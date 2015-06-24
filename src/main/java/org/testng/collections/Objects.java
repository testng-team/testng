package org.testng.collections;

import org.testng.util.Strings;

import java.util.List;


public final class Objects {

  private Objects() {}

  private static class ValueHolder {
    private String m_name;
    private String m_value;

    public ValueHolder(String name, String value) {
      m_name = name;
      m_value = value;
    }

    boolean isNull() {
      return m_value == null;
    }

    @Override
    public String toString() {
      return m_name + "=" + m_value;
    }

    public boolean isEmptyString() {
      return Strings.isNullOrEmpty(m_value);
    }
  }

  public static class ToStringHelper {
    private String m_className;
    private List<ValueHolder> values = Lists.newArrayList();
    private boolean m_omitNulls = false;
    private boolean m_omitEmptyStrings = false;

    public ToStringHelper(String className) {
      m_className = className;
    }

    public ToStringHelper omitNulls() {
      m_omitNulls = true;
      return this;
    }

    public ToStringHelper omitEmptyStrings() {
      m_omitEmptyStrings = true;
      return this;
    }

    public ToStringHelper add(String name, String value) {
      values.add(new ValueHolder(name, s(value)));
      return this;
    }

    public ToStringHelper add(String name, Object value) {
      values.add(new ValueHolder(name, s(value)));
      return this;
    }

    private String s(Object o) {
      return o != null
          ? (o.toString().isEmpty() ? "\"\"" : o.toString())
          : "{null}";
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder("[" + m_className + " ");
      for (int i = 0; i < values.size(); i++) {
        ValueHolder vh = values.get(i);
        if (m_omitNulls && vh.isNull()) continue;
        if (m_omitEmptyStrings && vh.isEmptyString()) continue;

        if (i > 0) {
          result.append(" ");
        }
        result.append(vh.toString());
      }
      result.append("]");

      return result.toString();
    }
  }

  public static ToStringHelper toStringHelper(Class<?> class1) {
    return new ToStringHelper(class1.getSimpleName());
  }

}
