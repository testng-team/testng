package org.testng.factory.github1131;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.factory.samples.github1131.EmptyConstructorSample;
import org.testng.factory.samples.github1131.IntConstructorSample;
import org.testng.factory.samples.github1131.StringConstructorSample;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class GitHub1131Test extends SimpleBaseTest {

  @Test(description = "GITHUB-1131")
  public void testFactoryOnEmptyConstructor() {
    EmptyConstructorSample.count = 0;
    TestNG tng = create(EmptyConstructorSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("test", "test");
    assertThat(EmptyConstructorSample.count).isEqualTo(2);
  }

  @Test(description = "GITHUB-1131")
  public void testFactoryOnIntConstructor() {
    IntConstructorSample.parameters.clear();
    TestNG tng = create(IntConstructorSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("test", "test");
    assertThat(IntConstructorSample.parameters).containsExactly(1, 2);
  }

  @Test(description = "GITHUB-1131")
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
