package test.beforegroups.issue2229;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class TestClassSample {

  public static List<String> logs = new ArrayList<>();

  boolean valueA = false;
  boolean valueB = false;

  @BeforeGroups(groups = "groupA")
  public void beforeGroupA() {
    logs.add("beforeGroupA");
    valueA = true;
  }

  @BeforeGroups(groups = "groupB")
  public void beforeGroupB() {
    valueB = true;
    logs.add("beforeGroupB");
  }

  @BeforeGroups(groups = "groupC")
  public void beforeGroupC() {
    logs.add("beforeGroupC No Test exist, should not run.");
  }

  @Test
  public void testA() {
    logs.add("TestA");
  }

  @Test
  public void testB() {
    logs.add("TestB");
  }

  @Test
  public void testC() {
    logs.add("TestC");
  }

  @Test(groups = "groupA")
  public void testGroupA1() {
    logs.add("testGroupA1");
    assertThat(valueA).withFailMessage("BeforeGroupA was not executed").isTrue();
  }

  @Test(groups = "groupA")
  public void testGroupA2() {
    logs.add("testGroupA2");
    assertThat(valueA).withFailMessage("BeforeGroupA was not executed").isTrue();
  }

  @Test(groups = "groupA")
  public void testGroupA3() {
    logs.add("testGroupA3");
    assertThat(valueA).withFailMessage("BeforeGroupA was not executed").isTrue();
  }

  @Test(groups = "groupB")
  public void testGroupB() {
    logs.add("testGroupB");
    assertThat(valueB).withFailMessage("BeforeGroupB was not executed").isTrue();
  }

  @AfterGroups(groups = "groupA")
  public void afterGroupA() {
    logs.add("afterGroupA");
    valueA = false;
  }

  @AfterGroups(groups = "groupB")
  public void afterGroupB() {
    logs.add("afterGroupB");
    valueB = false;
  }

  @AfterClass
  public void afterClass() {
    assertThat(valueA).withFailMessage("AfterGroupsA was not executed").isFalse();
    assertThat(valueB).withFailMessage("AfterGroupsB was not executed").isFalse();
  }
}
