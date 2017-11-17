package org.testng.internal.dynamicgraph;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class EdgeWeightTestSample2 {

    public static List<String> testCaseExecutionOrder = Lists.newArrayList();

    @Test(priority = 1)
    public void t1() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

    @Test(groups = { "group2" }, priority = 2)
    public void t2() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

    @Test(groups = { "group3" }, dependsOnGroups = "group2", priority = 3)
    public void t3() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

    @Test(groups = { "group4" }, dependsOnGroups = "group3", priority = 0)
    public void t4() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

    @Test(groups = { "group4" }, dependsOnGroups = "group3", priority = 1)
    public void t5() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

}
