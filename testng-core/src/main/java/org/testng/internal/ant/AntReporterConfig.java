package org.testng.internal.ant;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.collections.Lists;

/**
 * Used with the &lt;reporter&gt; sub-element of the Ant task
 *
 * <p>NOTE: this class needs to be public. It's used by TestNG Ant task
 */
public class AntReporterConfig {

  /** The class name of the reporter listener */
  protected String className;

  /** The properties of the reporter listener */
  private final List<Property> properties = Lists.newArrayList();

  public void addProperty(Property property) {
    properties.add(property);
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String serialize() {
    List<org.testng.internal.ReporterConfig.Property> properties =
        this.properties.stream()
            .map(
                property ->
                    new org.testng.internal.ReporterConfig.Property(property.name, property.value))
            .collect(Collectors.toList());
    return (new org.testng.internal.ReporterConfig(className, properties)).serialize();
  }

  public static class Property {
    private String name;
    private String value;

    public void setName(String name) {
      this.name = name;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
