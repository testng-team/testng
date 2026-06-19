package test.listeners.github1284;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class Sample1284B {
  @Test
  public void testTheOrderOfInvokedMethods() {
    assertThat(Listener1284.getInstance()).isNotNull();
    assertThat(Listener1284.testList).hasSize(5);

    String b1 = Sample1284.class.getName() + " - Before Invocation";
    String a1 = Sample1284.class.getName() + " - After Invocation";
    String b2 = Sample1284B.class.getName() + " - Before Invocation";

    List<String> expectedList = Lists.newArrayList(b1, a1, b1, a1, b2);
    assertThat(Listener1284.testList).containsExactlyElementsOf(expectedList);
  }
}
