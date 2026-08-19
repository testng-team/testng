package org.testng.internal.invokers;

import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
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

  /**
   * A group configuration is always tied to the test method that triggered it, so this narrows the
   * inherited contract back. See {@link TestMethodArguments#getTestMethod()} for why the base is
   * nullable at all.
   */
  @Override
  public ITestNGMethod getTestMethod() {
    return Objects.requireNonNull(super.getTestMethod());
  }

  /** Always present, for the same reason as {@link #getTestMethod()}. */
  @Override
  public Object getInstance() {
    return Objects.requireNonNull(super.getInstance());
  }

  public XmlSuite getSuite() {
    return Objects.requireNonNull(
            getTestMethod().getXmlTest(), "a grouped configuration method belongs to a <test>")
        .getSuite();
  }

  public static class Builder {

    private @Nullable ITestNGMethod testMethod;
    private @Nullable ConfigurationGroupMethods groupMethods;
    private @Nullable Map<String, String> params;
    private @Nullable Object instance;

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

    public Builder forInstance(@Nullable Object instance) {
      this.instance = instance;
      return this;
    }

    public GroupConfigMethodArguments build() {
      return new GroupConfigMethodArguments(
          Objects.requireNonNull(testMethod),
          Objects.requireNonNull(groupMethods),
          Objects.requireNonNull(params),
          Objects.requireNonNull(instance));
    }
  }
}
