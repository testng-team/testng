package org.testng;

import java.util.List;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.PropertyUtils;
import org.testng.internal.Utils;

/**
 * Stores the information regarding the configuration of a pluggable report listener. Used also in
 * conjunction with the &lt;reporter&gt; sub-element of the Ant task
 *
 * <p>NOTE: this class needs to be public. It's used by TestNG Ant task
 */
public class ReporterConfig {

  /** The class name of the reporter listener */
  private String m_className;

  /** The properties of the reporter listener */
  private final List<Property> m_properties = Lists.newArrayList();

  public void addProperty(Property property) {
    m_properties.add(property);
  }

  public List<Property> getProperties() {
    return m_properties;
  }

  public String getClassName() {
    return m_className;
  }

  public void setClassName(String className) {
    this.m_className = className;
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();
    sb.append(m_className);
    if (!m_properties.isEmpty()) {
      sb.append(":");

      for (int i = 0; i < m_properties.size(); i++) {
        ReporterConfig.Property property = m_properties.get(i);
        sb.append(property.name);
        sb.append("=");
        sb.append(property.value);
        if (i < m_properties.size() - 1) {
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

    ReporterConfig reporterConfig = new ReporterConfig();

    int clsNameEndIndex = inputString.indexOf(':');
    if (clsNameEndIndex == -1) {
      reporterConfig.setClassName(inputString);
    } else {
      reporterConfig.setClassName(inputString.substring(0, clsNameEndIndex));
      String propString = inputString.substring(clsNameEndIndex + 1, inputString.length());
      String[] props = propString.split(",");
      for (String prop : props) {
        String[] propNameAndVal = prop.split("=");
        if (propNameAndVal.length == 2) {
          reporterConfig.addProperty(new Property(propNameAndVal[0], propNameAndVal[1]));
        }
      }
    }

    return reporterConfig;
  }

  /** Creates a reporter based on the current configuration */
  public IReporter newReporterInstance() {

    Class<?> reporterClass = ClassHelper.forName(m_className);
    if (reporterClass == null) {
      return null;
    }

    Object tmp = ClassHelper.newInstance(reporterClass);
    if (!(tmp instanceof IReporter)) {
      throw new TestNGException(m_className + " is not a IReporter");
    }

    IReporter result = (IReporter) tmp;
    for (ReporterConfig.Property property : m_properties) {
      PropertyUtils.setProperty(result, property.name, property.value);
    }
    return result;
  }

  public static class Property {
    private final String name;
    private final String value;

    public Property(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("\nClass = ").append(m_className);
    for (Property prop : m_properties) {
      buf.append("\n\t ").append(prop.name).append("=").append(prop.value);
    }
    return buf.toString();
  }
}
