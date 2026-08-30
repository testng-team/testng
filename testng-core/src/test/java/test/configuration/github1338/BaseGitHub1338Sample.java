package test.configuration.github1338;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;

/**
 * Records which instance each configuration method ran on. The guarantee GITHUB-1338 is about is
 * that {@code groupSetUp} runs on an instance of a class that contributes a selected method to
 * {@code group1}, not merely on the first class the {@code <test>} lists.
 */
public class BaseGitHub1338Sample {

  private static final List<String> invocations = new CopyOnWriteArrayList<>();

  public static List<String> invocations() {
    return invocations;
  }

  public static void reset() {
    invocations.clear();
  }

  @BeforeClass(alwaysRun = true)
  public void classSetUp() {
    invocations.add("classSetUp:" + getClass().getSimpleName());
  }

  @BeforeGroups(
      groups = {"group1"},
      alwaysRun = true)
  public void groupSetUp() {
    invocations.add("groupSetUp:" + getClass().getSimpleName());
  }
}
