package org.testng.internal.invokers;

import java.util.Map;
import org.testng.ITestNGMethod;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.xml.XmlSuite;

public class GroupConfigMethodArguments extends Arguments {

  private final ConfigurationGroupMethods groupMethods;

  private GroupConfigMethodArguments(
      ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods,
      Map<String, String> params,
      Object instance) {
    super(instance, testMethod, params);
    this.groupMethods = groupMethods;
  }

  public ConfigurationGroupMethods getGroupMethods() {
    return groupMethods;
  }

  public XmlSuite getSuite() {
    return getTestMethod().getXmlTest().getSuite();
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

    public Builder withParameters(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public Builder forInstance(Object instance) {
      this.instance = instance;
      return this;
    }

    public GroupConfigMethodArguments build() {
      return new GroupConfigMethodArguments(testMethod, groupMethods, params, instance);
    }
  }
}
