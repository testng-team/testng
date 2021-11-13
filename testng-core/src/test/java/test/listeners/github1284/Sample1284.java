package test.listeners.github1284;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class Sample1284 {
  @Test
  public void testWithNoListener() {
    Assert.assertNull(Listener1284.getInstance());
    Assert.assertEquals(Listener1284.testList.size(), 0);
  }

  @Test
  public void testWithListener() {
    Assert.assertNotNull(Listener1284.getInstance());
    Assert.assertEquals(Listener1284.testList.size(), 1);
    Assert.assertEquals(
        Listener1284.testList.get(0), Sample1284.class.getName() + " - Before Invocation");
  }

  @Test
  public void testWithChildListener() {
    Assert.assertNotNull(Listener1284.getInstance());
    Assert.assertEquals(Listener1284.testList.size(), 3);

    String beforeInvocation = Sample1284.class.getName() + " - Before Invocation";
    String afterInvocation = Sample1284.class.getName() + " - After Invocation";
    List<String> expectedList =
        Lists.newArrayList(beforeInvocation, afterInvocation, beforeInvocation);

    Assert.assertEquals(Listener1284.testList, expectedList);
  }
}
