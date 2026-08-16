package org.testng.collections;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public final class Objects {

  private Objects() {}

  private static class ValueHolder {
    private final String m_name;
    private final @Nullable Object m_value;

    ValueHolder(String name, @Nullable Object value) {
      m_name = name;
      m_value = value;
    }

    boolean isNull() {
      return m_value == null;
    }

    boolean isEmptyString() {
      return m_value instanceof String && ((String) m_value).isEmpty();
    }

    @Override
    public String toString() {
      return m_name + "=" + format(m_value);
    }
  }

  public static class ToStringHelper {
    private final String m_className;
    private final List<ValueHolder> values = new ArrayList<>();
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

    public ToStringHelper add(String name, @Nullable String value) {
      values.add(new ValueHolder(name, value));
      return this;
    }

    public ToStringHelper add(String name, @Nullable Object value) {
      values.add(new ValueHolder(name, value));
      return this;
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder("[" + m_className + " ");
      boolean emitted = false;
      for (ValueHolder vh : values) {
        if (m_omitNulls && vh.isNull()) {
          continue;
        }
        if (m_omitEmptyStrings && vh.isEmptyString()) {
          continue;
        }
        if (emitted) {
          result.append(" ");
        }
        result.append(vh.toString());
        emitted = true;
      }
      result.append("]");

      return result.toString();
    }
  }

  public static ToStringHelper toStringHelper(Class<?> class1) {
    return new ToStringHelper(class1.getSimpleName());
  }

  private static String format(@Nullable Object value) {
    if (value == null) {
      return "{null}";
    }
    String text = value.toString();
    return text.isEmpty() ? "\"\"" : text;
  }
}
