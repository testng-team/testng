package test.inject.parameterresolver;

/** A type TestNG knows nothing about, so only a resolver can supply one. */
public class CustomObject {

  private final String value;

  public CustomObject(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "CustomObject(" + value + ")";
  }
}
