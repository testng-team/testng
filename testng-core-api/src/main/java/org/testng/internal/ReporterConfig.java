package org.testng.internal;

import java.util.List;
import org.testng.collections.Lists;

/** Stores the information regarding the configuration of a pluggable report listener. */
public class ReporterConfig {

  /** The class name of the reporter listener */
  // TODO make it private/final once org.testng.ReporterConfig will be removed
  protected String className;

  /** The properties of the reporter listener */
  // TODO make it private once org.testng.ReporterConfig will be removed
  protected final List<Property> properties;

  public ReporterConfig(String className, List<Property> properties) {
    this.className = className;
    this.properties = properties;
  }

  public String getClassName() {
    return className;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder(className);
    if (!properties.isEmpty()) {
      sb.append(":");

      for (int i = 0; i < properties.size(); i++) {
        Property property = properties.get(i);
        sb.append(property);
        if (i < properties.size() - 1) {
          sb.append(",");
        }
      }
    }
    return sb.toString();
  }

  public static ReporterConfig deserialize(String inputString) {

    if (Utils.isStringEmpty(inputString)) {
      return null;
    }

    String className;
    List<Property> properties = Lists.newArrayList();
    int clsNameEndIndex = inputString.indexOf(':');
    if (clsNameEndIndex == -1) {
      className = inputString;
    } else {
      className = inputString.substring(0, clsNameEndIndex);
      String propString = inputString.substring(clsNameEndIndex + 1);
      for (String prop : propString.split(",")) {
        String[] propNameAndVal = prop.split("=");
        if (propNameAndVal.length == 2) {
          properties.add(new Property(propNameAndVal[0], propNameAndVal[1]));
        }
      }
    }

    return new ReporterConfig(className, properties);
  }

  public static class Property {
    private final String name;
    private final String value;

    public Property(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return name + "=" + value;
    }
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("\nClass = ").append(className);
    for (Property prop : properties) {
      buf.append("\n\t ").append(prop);
    }
    return buf.toString();
  }
}
