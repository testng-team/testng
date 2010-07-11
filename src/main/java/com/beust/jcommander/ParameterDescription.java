package com.beust.jcommander;

import org.testng.collections.Lists;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class ParameterDescription {
  private Object m_object;
  private Parameter m_parameterAnnotation;
  private Field m_field;
  /** Keep track of whether a value was added to flag an error */
  private boolean m_added = false;

  public ParameterDescription(Object object, Parameter annotation, Field field) {
    m_object = object;
    m_parameterAnnotation = annotation;
    m_field = field;
  }

  public String getName() {
    return m_parameterAnnotation.name();
  }

  public String getDescription() {
    return m_parameterAnnotation.description();
  }

  public Field getField() {
    return m_field;
  }

  private boolean isMultiOption() {
    Class<?> fieldType = m_field.getType();
    return fieldType.equals(List.class) || fieldType.equals(Set.class);
  }

  public void addValue(Object value) {
    if (m_added && ! isMultiOption()) {
      throw new ParameterException("Can only specify option " + getName() + " once.");
    }

    m_added = true;
    try {
      Class<?> fieldType = m_field.getType();
      if (fieldType.equals(String.class)) {
        m_field.set(m_object, value);
      } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
        m_field.set(m_object, Integer.parseInt(value.toString()));
      } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
        m_field.set(m_object, Long.parseLong(value.toString()));
      } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
        m_field.set(m_object, Float.parseFloat(value.toString()));
      } else if (isMultiOption()) {
        List l = (List) m_field.get(m_object);
        if (l == null) {
          l = Lists.newArrayList();
          m_field.set(m_object, l);
        }
        l.add(value);
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
