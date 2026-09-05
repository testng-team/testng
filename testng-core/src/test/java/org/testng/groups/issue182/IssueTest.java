package org.testng.groups.issue182;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.groups.samples.issue182.ChildTest;
import org.testng.groups.samples.issue182.LocalListener;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  private static final String MY_GROUP = "myGroup";

  @Test
  public void ensureGroupsPresentInInheritedMethods() {
    TestNG testng = create(ChildTest.class);
    LocalListener listener = new LocalListener();
    testng.addListener(listener);
    testng.run();
    Map<String, List<String>> expected = new HashMap<>();
    expected.put("parentTestMethod", Collections.singletonList(MY_GROUP));
    expected.put("childTestMethod", Collections.singletonList(MY_GROUP));
    assertThat(listener.getMapping()).containsAllEntriesOf(expected);
  }
}
