package test.listeners.github1284;

import static org.assertj.core.api.Assertions.assertThat;
import static test.listeners.github1284.Listener1284.getInstance;

import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

public class Sample1284B {
  @Test
  public void testTheOrderOfInvokedMethods() {
    assertThat(getInstance()).isNotNull();
    assertThat(Listener1284.testList.size()).isEqualTo(5);

    String b1 = Sample1284.class.getName() + " - Before Invocation";
    String a1 = Sample1284.class.getName() + " - After Invocation";
    String b2 = Sample1284B.class.getName() + " - Before Invocation";

    List<String> expectedList = Arrays.asList(b1, a1, b1, a1, b2);
    assertThat(Listener1284.testList).isEqualTo(expectedList);
  }
}
