package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import test.SimpleBaseTest;

public class FactoryInterleavingTest extends SimpleBaseTest {

  public static List<Integer> LOG = Lists.newArrayList();

  @Test
  public void methodsShouldBeInterleaved() {
    TestNG tng = create(InterleavingFactorySample.class);
    tng.setPreserveOrder(false);
    tng.run();
    Integer[] valid1 = {
      10, 11, 12, 13,
      20, 21, 22, 23,
    };

    Integer[] valid2 = {
      20, 21, 22, 23,
      10, 11, 12, 13,
    };
    Integer[] logArray = LOG.toArray(new Integer[0]);
    assertThat(logArray)
        .satisfiesAnyOf(
            it -> assertThat(it).isEqualTo(valid1), it -> assertThat(it).isEqualTo(valid2));
  }
}
