package test.inject.parameterresolver;

/** An interface, so that a mock of it is a proxy rather than a subclass. */
public interface Greeter {
  String greet(String name);
}
