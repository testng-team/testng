package test.cli.github2974;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.CommandLineArgs;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class OverrideGroupsCliTest extends SimpleBaseTest {

  private static class NameCollector implements ITestListener {
    List<String> names = new ArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
      names.add(result.getName());
    }
  }

  @Test(description = "GITHUB-2974")
  public void overrideIncludeGroupsFromCliInParentChildXml() {
    TestNG testNG =
        new TestNG(false) {
          {
            CommandLineArgs args = new CommandLineArgs();
            args.groups = "override_group";
            args.suiteFiles = List.of(getPathToResource("2974/parent_include.xml"));
            configure(args);
          }
        };
    NameCollector collector = new NameCollector();
    testNG.addListener(collector);
    testNG.run();
    Assert.assertTrue(collector.names.contains("overrideTest"));
    Assert.assertFalse(collector.names.contains("defaultTest"));
  }

  @Test(description = "GITHUB-2974")
  public void overrideExcludeGroupsFromCliInParentChildXml() {
    TestNG testNG =
        new TestNG(false) {
          {
            CommandLineArgs args = new CommandLineArgs();
            args.excludedGroups = "override_group";
            args.suiteFiles = List.of(getPathToResource("2974/parent_exclude.xml"));
            configure(args);
          }
        };
    NameCollector collector = new NameCollector();
    testNG.addListener(collector);
    testNG.run();
    Assert.assertTrue(collector.names.contains("defaultTest"));
    Assert.assertFalse(collector.names.contains("overrideTest"));
  }
}
