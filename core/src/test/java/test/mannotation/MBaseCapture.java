package test.mannotation;

import org.testng.annotations.Test;

/**
 * Make sure that if a method is declared in the base class and a child class
 * adds a class-scoped group to it, that method receives it as well.
 *
 * @author cbeust
 * @date Mar 22, 2006
 */
public class MBaseCapture {

  @Test
  public void shouldBelongToGroupChild() {

  }
}
