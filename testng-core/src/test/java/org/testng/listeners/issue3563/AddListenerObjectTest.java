package org.testng.listeners.issue3563;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;

public class AddListenerObjectTest {

  @Test
  @SuppressWarnings("deprecation")
  public void addListenerThrowsWhenTheObjectIsNotAListener() {
    TestNG testng = new TestNG();

    assertThatThrownBy(() -> testng.addListener(new Object()))
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("must be one of");
  }

  @Test
  @SuppressWarnings("deprecation")
  public void addListenerKeepsAListener() {
    TestNG testng = new TestNG();
    TestListenerAdapter listener = new TestListenerAdapter();

    testng.addListener((Object) listener);

    assertThat(testng.getTestListeners()).contains(listener);
  }
}
