package test.guice.issue279;

public class TextGreeter implements Greeter {

  @Override
  public String greet(String name) {
    return "hello " + name;
  }
}
