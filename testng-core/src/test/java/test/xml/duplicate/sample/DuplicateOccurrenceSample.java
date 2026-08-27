package test.xml.duplicate.sample;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/** Two parameterised methods, so a &lt;class&gt; occurrence can select one of them. */
public class DuplicateOccurrenceSample {

  @Test
  @Parameters("id")
  public void f(String id) {}

  @Test
  @Parameters("id")
  public void g(String id) {}
}
