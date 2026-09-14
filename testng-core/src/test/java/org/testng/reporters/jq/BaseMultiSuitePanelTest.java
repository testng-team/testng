package org.testng.reporters.jq;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.testng.ISuite;
import org.testng.annotations.Test;
import org.testng.reporters.XMLStringBuffer;

/**
 * That {@code getContent} cannot be overridden.
 *
 * <p>{@code generate} used to call it, so a panel outside this package decided what it emitted by
 * overriding it. It calls the package-private {@code writeContent} now, so the same override
 * compiles, runs, and is never consulted -- which is a defect no test can observe from inside the
 * package, both routes ending at {@code writeContent}. Refusing the override is what turns that
 * silence into a compile error, and this is what pins it.
 */
public class BaseMultiSuitePanelTest {

  @Test(description = "getContent cannot be overridden, since the report would not consult it")
  public void getContentIsFinal() throws Exception {
    Method getContent =
        BaseMultiSuitePanel.class.getMethod("getContent", ISuite.class, XMLStringBuffer.class);

    assertThat(Modifier.isFinal(getContent.getModifiers()))
        .as("getContent is a hook the report no longer calls, so it has to refuse overriding")
        .isTrue();
  }
}
