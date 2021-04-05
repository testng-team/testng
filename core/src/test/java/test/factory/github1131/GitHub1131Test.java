package test.factory.github1131;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHub1131Test extends SimpleBaseTest {

  @Test
  public void testFactoryOnEmptyConstructor() {
    EmptyConstructorSample.count = 0;
    TestNG tng = create(EmptyConstructorSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("test", "test");
    Assert.assertEquals(EmptyConstructorSample.count, 2);
  }

  @Test
  public void testFactoryOnIntConstructor() {
    IntConstructorSample.parameters.clear();
    TestNG tng = create(IntConstructorSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("test", "test");
    assertThat(IntConstructorSample.parameters).containsExactly(1, 2);
  }

  @Test
  public void testFactoryOnStringConstructor() {
    StringConstructorSample.parameters.clear();
    TestNG tng = create(StringConstructorSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("test", "test");
    assertThat(StringConstructorSample.parameters).containsExactly("foo", "bar");
  }
}
