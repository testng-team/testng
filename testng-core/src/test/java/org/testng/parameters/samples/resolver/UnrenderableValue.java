package org.testng.parameters.samples.resolver;

/** A value that cannot describe itself, which the diagnostic naming it must survive. */
public class UnrenderableValue {

  @Override
  public String toString() {
    throw new IllegalStateException("toString() blew up");
  }
}
