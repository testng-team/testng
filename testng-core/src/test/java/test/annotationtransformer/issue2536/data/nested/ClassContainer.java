package test.annotationtransformer.issue2536.data.nested;

import org.testng.annotations.Test;

public class ClassContainer {

  @Test
  public class NonGroupClass3 {

    public void step1() {}

    public void step2() {}
  }
}
