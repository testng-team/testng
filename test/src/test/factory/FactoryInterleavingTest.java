package test.factory;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class FactoryInterleavingTest extends SimpleBaseTest {

  public static List<Integer> LOG = Lists.newArrayList();

  @Test
  public void methodsShouldBeInterleaved() {
    TestNG tng = create(FactoryInterleavingSampleFactory.class);
    tng.run();
    Integer[] valid1 = {
        10, 11, 12, 13,
        20, 21, 22, 23,
    };

    Integer[] valid2 = {
        20, 21, 22, 23,
        10, 11, 12, 13,
    };
    Integer[] logArray = LOG.toArray(new Integer[LOG.size()]);
    Assert.assertTrue(Arrays.equals(logArray, valid1) || Arrays.equals(logArray, valid2));
  }
}
