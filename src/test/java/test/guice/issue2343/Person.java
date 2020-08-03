package test.guice.issue2343;

public class Person {

  public static int counter;

  public Person() {
    ++counter;
  }
}
