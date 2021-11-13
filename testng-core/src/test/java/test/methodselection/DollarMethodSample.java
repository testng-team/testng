package test.methodselection;

import org.testng.annotations.Test;

public class DollarMethodSample {

  @Test
  public void $() {}

  @Test
  public void another_test() {}

  @Test
  public void another$_test() {}

  @Test
  public void another$_test$() {}

  @Test
  public void $another$_test$() {}
}
