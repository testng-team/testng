package test.listeners.issue2578;

import org.testng.ITestListener;

public class ListenerWithMissingConstructorDependency implements ITestListener {

  public ListenerWithMissingConstructorDependency() {}

  public ListenerWithMissingConstructorDependency(MissingConstructorDependency dependency) {}
}
