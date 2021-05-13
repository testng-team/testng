package test.ignore;

import org.testng.annotations.Ignore;

@Ignore
public class ChildClassTestSample extends ParentClassTestSample {
  @Override
  protected void hook() {}
}
