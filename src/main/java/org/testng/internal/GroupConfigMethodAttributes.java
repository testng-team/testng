package org.testng.internal;

import java.util.Map;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlSuite;

public class GroupConfigMethodAttributes {

  private final ITestNGMethod testMethod;
  private final ConfigurationGroupMethods groupMethods;
  private final XmlSuite suite;
  private final Map<String, String> params;
  private final Object instance;

  private GroupConfigMethodAttributes(ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods, XmlSuite suite, Map<String, String> params,
      Object instance) {
    this.testMethod = testMethod;
    this.groupMethods = groupMethods;
    this.suite = suite;
    this.params = params;
    this.instance = instance;
  }

  public ITestNGMethod getTestMethod() {
    return testMethod;
  }

  public ConfigurationGroupMethods getGroupMethods() {
    return groupMethods;
  }

  public XmlSuite getSuite() {
    return suite;
  }

  public Map<String, String> getParameters() {
    return params;
  }

  public Object getInstance() {
    return instance;
  }

  public static class Builder {

    private ITestNGMethod testMethod;
    private ConfigurationGroupMethods groupMethods;
    private XmlSuite suite;
    private Map<String, String> params;
    private Object instance;

    public Builder forTestMethod(ITestNGMethod testMethod) {
      this.testMethod = testMethod;
      return this;
    }

    public Builder withGroupConfigMethods(ConfigurationGroupMethods groupMethods) {
      this.groupMethods = groupMethods;
      return this;
    }

    public Builder forSuite(XmlSuite suite) {
      this.suite = suite;
      return this;
    }

    public Builder withParameters(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public Builder forInstance(Object instance) {
      this.instance = instance;
      return this;
    }

    public GroupConfigMethodAttributes build() {
      return new GroupConfigMethodAttributes(testMethod, groupMethods, suite, params, instance);
    }
  }
}
