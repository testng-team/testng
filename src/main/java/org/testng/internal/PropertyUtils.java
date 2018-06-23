package org.testng.internal;

import org.testng.log4testng.Logger;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for setting JavaBeans-style properties on instances.
 *
 * @author Cosmin Marginean, Apr 12, 2007
 */
public class PropertyUtils {

  private static final Logger LOGGER = Logger.getLogger(PropertyUtils.class);

  public static void setProperty(Object instance, String name, String value) {
    if (instance == null) {
      LOGGER.warn(
          "Cannot set property " + name + " with value " + value + ". The target instance is null");
      return;
    }

    Class propClass = getPropertyType(instance.getClass(), name);
    if (propClass == null) {
      LOGGER.warn(
          "Cannot set property "
              + name
              + " with value "
              + value
              + ". Property class could not be found");
      return;
    }

    Object realValue = Parameters.convertType(propClass, value, name);
    // TODO: Here the property desc is serched again
    setPropertyRealValue(instance, name, realValue);
  }

  public static Class getPropertyType(Class instanceClass, String propertyName) {
    if (instanceClass == null) {
      LOGGER.warn(
          "Cannot retrieve property class for " + propertyName + ". Target instance class is null");
    }
    PropertyDescriptor propDesc = getPropertyDescriptor(instanceClass, propertyName);
    if (propDesc == null) {
      return null;
    }
    return propDesc.getPropertyType();
  }

  private static PropertyDescriptor getPropertyDescriptor(Class targetClass, String propertyName) {
    PropertyDescriptor result = null;
    if (targetClass == null) {
      LOGGER.warn("Cannot retrieve property " + propertyName + ". Class is null");
    } else {
      try {
        BeanInfo beanInfo = Introspector.getBeanInfo(targetClass);
        PropertyDescriptor[] propDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propDesc : propDescriptors) {
          if (propDesc.getName().equals(propertyName)) {
            result = propDesc;
            break;
          }
        }
      } catch (IntrospectionException ie) {
        LOGGER.warn("Cannot retrieve property " + propertyName + ". Cause is: " + ie);
      }
    }
    return result;
  }

  public static void setPropertyRealValue(Object instance, String name, Object value) {
    if (instance == null) {
      LOGGER.warn(
          "Cannot set property " + name + " with value " + value + ". Targe instance is null");
      return;
    }

    PropertyDescriptor propDesc = getPropertyDescriptor(instance.getClass(), name);
    if (propDesc == null) {
      LOGGER.warn(
          "Cannot set property " + name + " with value " + value + ". Property does not exist");
      return;
    }

    Method method = propDesc.getWriteMethod();
    try {
      method.invoke(instance, value);
    } catch (IllegalAccessException | InvocationTargetException iae) {
      LOGGER.warn("Cannot set property " + name + " with value " + value + ". Cause " + iae);
    }
  }
}
