package org.testng.reporters.issue2725;

import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

public class TestClassSample {
  @Test(
      attributes = {
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-1"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-2"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-3"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-4"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-5"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-6"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-7"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-8"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-9"}),
        @CustomAttribute(
            name = "code_name",
            values = {"dragon_warrior-10"})
      })
  public void helloWorld() {}
}
