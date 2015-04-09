package test.groupbug;

import org.testng.Assert;
import org.testng.annotations.Test;

import test.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupBugTest extends BaseTest {

  static List<String> passed = new ArrayList<>();

  @Test(groups = "broken",
      description = "Comment out dependsOnGroups in ITCaseOne will fix the ordering, that's the bug")
  public void shouldOrderByClass() {
    passed.clear();
    addClass(ITCaseOne.class);
    addClass(ITCaseTwo.class);
    run();
    List<String> expected = Arrays.asList(
        "one1", "one2", "two1", "two2"
    );
    Assert.assertEquals(passed, expected);
  }
}
