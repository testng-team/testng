package org.testng.internal;

import java.util.Map;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlSuite;

public class GroupConfigMethodArguments extends Arguments {

  private final ConfigurationGroupMethods groupMethods;
  private final XmlSuite suite;

  private GroupConfigMethodArguments(ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods, XmlSuite suite, Map<String, String> params,
      Object instance) {
    super(instance, testMethod, params);
    this.groupMethods = groupMethods;
    this.suite = suite;
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

    public GroupConfigMethodArguments build() {
      return new GroupConfigMethodArguments(testMethod, groupMethods, suite, params, instance);
    }
  }
}
