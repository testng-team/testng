package test;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Test1 extends BaseTest {

  /**
   * This constructor is package protected on purpose, to test that
   * TestNG can still instantiate the class.
   */
  Test1() {}

  @Test(groups = { "current" })
  public void includedGroups() {
    addClass("test.sample.Sample1");
    assert 1 == getTest().getXmlClasses().size();
    addIncludedGroup("odd");
    run();
    String[] passed = {
      "method1", "method3",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void groupsOfGroupsSimple() {
    addClass("test.sample.Sample1");
    assert 1 == getTest().getXmlClasses().size();
    // should match all methods belonging to group "odd" and "even"
    addIncludedGroup("evenodd");
    List l = new ArrayList<>();
    l.add("even");
    l.add("odd");
    addMetaGroup("evenodd", l);
    run();
   String passed[] = {
    "method1", "method2", "method3",
   };
  String[] failed = {
  };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void groupsOfGroupsWithIndirections() {
    addClass("test.sample.Sample1");
    addIncludedGroup("all");
    List l = new ArrayList<>();
    l.add("methods");
    l.add("broken");
    addMetaGroup("all", l);
    l = new ArrayList<>();
    l.add("odd");
    l.add("even");
    addMetaGroup("methods", l);
    addMetaGroup("broken", "broken");
    run();
    String[] passed = {
      "method1", "method2", "method3", "broken"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void groupsOfGroupsWithCycle() {
    addClass("test.sample.Sample1");
    addIncludedGroup("all");
    addMetaGroup("all", "all2");
    addMetaGroup("all2", "methods");
    addMetaGroup("methods", "all");
    run();
    String[] passed = {
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test // (groups = { "one" })
  public void excludedGroups() {
    addClass("test.sample.Sample1");
    addExcludedGroup("odd");
    run();
   String passed[] = {
    "method2",
    "broken", "throwExpectedException1ShouldPass",
    "throwExpectedException2ShouldPass"
   };
  String[] failed = {
      "throwExceptionShouldFail", "verifyLastNameShouldFail"
  };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void regexp() {
    addClass("test.sample.Sample1");
    // should matches all methods belonging to group "odd"
    addIncludedGroup("o.*");
    run();
   String passed[] = {
      "method1", "method3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test(groups = { "currentold" })
  public void logger() {
    Logger logger = Logger.getLogger("");
//    System.out.println("# HANDLERS:" + logger.getHandlers().length);
    for (Handler handler : logger.getHandlers())
    {
      handler.setLevel(Level.WARNING);
      handler.setFormatter(new org.testng.log.TextFormatter());
    }
    logger.setLevel(Level.SEVERE);
  }

  static public void ppp(String s) {
    System.out.println("[Test1] " + s);
  }

} // Test1


