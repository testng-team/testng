package test.listeners.github1284;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class Sample1284 {
  @Test
  public void testWithNoListener() {
    assertThat(Listener1284.getInstance()).isNull();
    assertThat(Listener1284.testList.size()).isEqualTo(0);
  }

  @Test
  public void testWithListener() {
    assertThat(Listener1284.getInstance()).isNotNull();
    assertThat(Listener1284.testList.size()).isEqualTo(1);
    assertThat(Listener1284.testList.get(0))
        .isEqualTo(Sample1284.class.getName() + " - Before Invocation");
  }

  @Test
  public void testWithChildListener() {
    assertThat(Listener1284.getInstance()).isNotNull();
    assertThat(Listener1284.testList.size()).isEqualTo(3);

    String beforeInvocation = Sample1284.class.getName() + " - Before Invocation";
    String afterInvocation = Sample1284.class.getName() + " - After Invocation";
    List<String> expectedList =
        Lists.newArrayList(beforeInvocation, afterInvocation, beforeInvocation);

    assertThat(Listener1284.testList).isEqualTo(expectedList);
  }
}
