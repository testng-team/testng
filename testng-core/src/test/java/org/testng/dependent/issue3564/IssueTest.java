package org.testng.dependent.issue3564;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.DependencyMap;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/** GITHUB-3564. {@code DependencyMap.getMethodsThatBelongTo} returns each method once. */
public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3564")
  public void aMethodOfTheGroupIsReturnedOnce() {
    assertThat(methodsThatBelongTo("g2")).containsExactly("inTwoGroups");
  }

  @Test(description = "GITHUB-3564")
  public void aMethodInSeveralMatchingGroupsIsReturnedOnce() {
    assertThat(methodsThatBelongTo("g.*")).containsExactlyInAnyOrder("inOneGroup", "inTwoGroups");
  }

  private static List<String> methodsThatBelongTo(String group) {
    TestNG testng = create(GroupsSample.class);
    GroupLookup lookup = new GroupLookup(group);
    testng.addListener(lookup);
    testng.run();
    return lookup.names;
  }

  /** Asks a {@link DependencyMap} of the test methods of the run for the methods of a group. */
  public static class GroupLookup implements ITestListener {

    private final String group;
    private final List<String> names = new ArrayList<>();

    public GroupLookup(String group) {
      this.group = group;
    }

    @Override
    public void onStart(ITestContext context) {
      ITestNGMethod[] methods = context.getAllTestMethods();
      for (ITestNGMethod method :
          new DependencyMap(methods).getMethodsThatBelongTo(group, methods[0])) {
        names.add(method.getMethodName());
      }
    }
  }
}
