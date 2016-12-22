package org.testng.collections;

import org.testng.util.Strings;

import java.util.List;


public final class Objects {

  private Objects() {}

  private static class ValueHolder {
    private String name;
    private String value;

    public ValueHolder(String name, String value) {
      this.name = name;
      this.value = value;
    }

    boolean isNull() {
      return value == null;
    }

    @Override
    public String toString() {
      return name + "=" + value;
    }

    public boolean isEmptyString() {
      return Strings.isNullOrEmpty(value);
    }
  }

  public static class ToStringHelper {
    private String className;
    private List<ValueHolder> values = Lists.newArrayList();
    private boolean omitNulls = false;
    private boolean omitEmptyStrings = false;

    public ToStringHelper(String className) {
      this.className = className;
    }

    public ToStringHelper omitNulls() {
      omitNulls = true;
      return this;
    }

    public ToStringHelper omitEmptyStrings() {
      omitEmptyStrings = true;
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
      StringBuilder result = new StringBuilder("[" + className + " ");
      for (int i = 0; i < values.size(); i++) {
        ValueHolder vh = values.get(i);
        if (omitNulls && vh.isNull()) continue;
        if (omitEmptyStrings && vh.isEmptyString()) continue;

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
