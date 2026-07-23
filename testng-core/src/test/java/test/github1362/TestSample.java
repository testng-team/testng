package test.github1362;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class TestSample {

  @BeforeGroups(groups = {"exTests"})
  public void setup() {}

  @Test(groups = {"exTests"})
  public void test1() {
    assertThat(true).withFailMessage("test1").isTrue();
  }

  @Test(groups = {"exTests"})
  public void test2() {
    assertThat(true).withFailMessage("test2").isTrue();
  }

  @Test(groups = {"exTests"})
  public void test3() {
    assertThat(true).withFailMessage("test3").isTrue();
  }

  @AfterGroups(groups = {"exTests"})
  public void clear() {}
}
