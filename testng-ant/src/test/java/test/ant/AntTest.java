package test.ant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.apache.tools.ant.BuildFileRule;
import org.testng.annotations.Test;

public class AntTest {

  private final BuildFileRule rule = new BuildFileRule();

  @Test
  public void testSimple() {
    rule.configureProject("src/test/resources/ant/build-simple.xml");
    rule.executeTarget("testng");
    String expectedText = "Total tests run: 1, Passes: 1, Failures: 0, Skips: 0";
    assertThat(rule.getLog()).containsPattern(Pattern.compile(expectedText));
  }

  @Test
  public void testReporter() {
    MyReporter.expectedFilter = "*insert*";
    MyReporter.expectedFiltering = true;

    rule.configureProject("src/test/resources/ant/build-reporter-config.xml");
    rule.executeTarget("testng");
  }
}
