package test.groupinvocation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class DummyTest {
  private static final Map<String, Integer> s_externalClassGroups = new HashMap<>();

  @Test(groups = {"a"})
  public void testA() {}

  @Test(groups = {"b"})
  public void testB() {}

  @Test(groups = {"a", "b"})
  public void testAB() {}

  @AfterClass(alwaysRun = true)
  public void checkInvocations() {
    Integer hashCode1 = s_externalClassGroups.get("beforeGroups");
    assertThat(hashCode1).withFailMessage("External @BeforeGroups not invoked").isNotNull();
    Integer hashCode2 = s_externalClassGroups.get("afterGroups");
    assertThat(hashCode2).withFailMessage("External @AfterGroups not invoked").isNotNull();
    assertThat(hashCode1)
        .withFailMessage(
            "External @BeforeGroups and @AfterGroups were not invoked on the"
                + " same class instance")
        .isEqualTo(hashCode2);
  }

  public static void recordInvocation(String string, int i) {
    s_externalClassGroups.put(string, i);
  }
}
