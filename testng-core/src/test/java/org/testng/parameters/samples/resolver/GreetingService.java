package org.testng.parameters.samples.resolver;

/** A service a Guice module binds, so a test method can ask for one by type. */
public interface GreetingService {
  String greet(String name);
}
