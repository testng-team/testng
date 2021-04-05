package test.ignore;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class IgnoreClassSample extends ParentSample {

  @Test
  public void test() {}

  @Test
  public void test2() {}
}
