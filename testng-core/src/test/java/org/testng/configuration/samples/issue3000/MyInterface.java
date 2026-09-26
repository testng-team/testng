package org.testng.configuration.samples.issue3000;

@SuppressWarnings("unused")
interface MyInterface {
  void setDependency(Object dependency);

  default Object getDependency() {
    return null;
  }
}
