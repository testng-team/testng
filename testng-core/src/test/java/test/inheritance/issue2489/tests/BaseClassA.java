package test.inheritance.issue2489.tests;

import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.collections.Lists;
import org.testng.internal.AutoCloseableLock;

public class BaseClassA {
  public static final List<String> logs = Lists.newArrayList();
  private final AutoCloseableLock lock = new AutoCloseableLock();

  @BeforeSuite(groups = "a")
  public void beforeSuite() {
    print("beforeSuite_BaseClass_A");
  }

  @AfterSuite(groups = "a")
  public void afterSuite() {
    print("afterSuite_BaseClass_A");
  }

  @BeforeClass(groups = "a")
  public void beforeAllClasses() {
    print("beforeClasses_BaseClass_A");
  }

  @AfterClass(groups = "a")
  public void afterAllClasses() {
    print("afterClasses_BaseClass_A");
  }

  protected void print(String message) {
    try (AutoCloseableLock ignore = lock.lock()) {
      logs.add(message);
    }
  }
}
