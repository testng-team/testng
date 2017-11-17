package org.testng.internal.dynamicgraph;

import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class EdgeWeightTestSample1 {

    public static List<String> testCaseExecutionOrder = Lists.newArrayList();

    @Test(groups = { "g1" }, priority = 2)
    public void t1() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

    @Test(groups = { "g2" }, dependsOnGroups = "g1", priority = 0)
    public void t2() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

    @Test(groups = { "g2" }, dependsOnGroups = "g1", priority = 1)
    public void t3() {
        testCaseExecutionOrder.add(new Object() {}.getClass().getEnclosingMethod().getName());
    }

}
