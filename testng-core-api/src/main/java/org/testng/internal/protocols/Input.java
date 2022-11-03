package org.testng.internal.protocols;

import java.util.Collections;
import java.util.List;

public class Input {

  private final List<String> included;
  private final List<String> excluded;
  private final String packageWithoutWildCards;
  private final boolean recursive;
  private final String packageDirName;
  private final String packageName;

  private Input(Builder builder) {
    included = Collections.unmodifiableList(builder.included);
    excluded = Collections.unmodifiableList(builder.excluded);
    packageWithoutWildCards = builder.packageWithoutWildCards;
    recursive = builder.recursive;
    packageName = builder.packageName;
    packageDirName = builder.packageDirName;
  }

  public List<String> getIncluded() {
    return included;
  }

  public List<String> getExcluded() {
    return excluded;
  }

  public String getPackageWithoutWildCards() {
    return packageWithoutWildCards;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public String getPackageDirName() {
    return packageDirName;
  }

  public String getPackageName() {
    return packageName;
  }

  public static final class Builder {
    private List<String> included;
    private List<String> excluded;
    private String packageWithoutWildCards;
    private boolean recursive;
    private String packageDirName;
    private String packageName;

    private Builder() {}

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder include(List<String> val) {
      included = val;
      return this;
    }

    public Builder exclude(List<String> val) {
      excluded = val;
      return this;
    }

    public Builder forPackageWithoutWildCards(String val) {
      packageWithoutWildCards = val;
      return this;
    }

    public Builder withRecursive(boolean val) {
      recursive = val;
      return this;
    }

    public Builder forPackageDirectory(String val) {
      packageDirName = val;
      return this;
    }

    public Builder withPackageName(String val) {
      packageName = val;
      return this;
    }

    public Input build() {
      return new Input(this);
    }
  }
}
